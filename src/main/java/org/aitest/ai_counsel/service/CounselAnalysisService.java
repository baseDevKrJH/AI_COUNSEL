package org.aitest.ai_counsel.service;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aitest.ai_counsel.domain.Counsel;
import org.aitest.ai_counsel.exception.AnalysisException;
import org.aitest.ai_counsel.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CounselAnalysisService {

    private final Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
    private OpenAiService openAiService;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @PostConstruct
    private void init() {
        // API 키가 유효한 경우에만 OpenAiService를 초기화합니다.
        if (openaiApiKey != null && !openaiApiKey.isEmpty() && !openaiApiKey.equals("YOUR_OPENAI_API_KEY")) {
            this.openAiService = new OpenAiService(openaiApiKey, Duration.ofSeconds(60));
        }
    }

    private static final Map<String, String> COUNSEL_TYPES = Map.of(
        "상품문의", "상품의 특성, 가격, 조건 등에 대한 문의",
        "불만���수", "서비스나 상품에 대한 불만 제기",
        "정보변경", "고객 정보 변경 요청",
        "해지요청", "서비스 해지나 계약 종료 요청",
        "일반상담", "기타 일반적인 문의사항"
    );

    /**
     * 상담 내용을 분석하여 결과�� 반환합니다.
     */
    public AnalysisResult analyzeCounsel(Counsel counsel) {
        return Optional.ofNullable(counsel)
                .map(c -> {
                    // 상담 내용 검증
                    String content = Optional.ofNullable(c.getContent())
                            .filter(text -> !text.trim().isEmpty())
                            .orElseThrow(() -> new InvalidRequestException("분석할 상담 내용이 비어있습니다."));

                    try {
                        // 키워드 추출
                        List<String> keywords = extractKeywords(content);

                        // 감정 분석
                        String sentiment = analyzeSentiment(content);

                        // 상담 유형 분류
                        String counselType = classifyCounselType(content);

                        return new AnalysisResult(keywords, sentiment, counselType);
                    } catch (Exception e) {
                        throw new AnalysisException("상담 내용 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
                    }
                })
                .orElseThrow(() -> new InvalidRequestException("분석할 상담 정보가 누락되었습니다."));
    }

    /**
     * 키워드를 추출합니다.
     */
    private List<String> extractKeywords(String content) {
        try {
            return Optional.ofNullable(content)
                    .map(text -> {
                        KomoranResult result = komoran.analyze(text);
                        return result.getTokenList().stream()
                                .filter(token -> token.getPos().matches("NN.*|VV.*|VA.*")) // 명사, 동사, 형용사만
                                .map(token -> token.getMorph())
                                .filter(word -> word.length() > 1) // 단일 문자 제외
                                .distinct()
                                .limit(10)
                                .collect(Collectors.toList());
                    })
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            throw new AnalysisException("키��드 추출 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 상담 유형을 분류합니다.
     */
    private String classifyCounselType(String content) {
        // 최소 점수 임계값
        final int THRESHOLD = 2;
        
        Map<String, Integer> typeScores = new HashMap<>();
        COUNSEL_TYPES.keySet().forEach(type -> typeScores.put(type, 0));
        
        // 1. 문장 패턴 기반 점수 계산
        if (content.contains("문의드립니다") || content.contains("알고 싶습니다") || 
            content.contains("어떻게 되나요") || content.contains("문의하고 싶")) {
            typeScores.merge("상품문의", 1, Integer::sum);
        }
        
        // 2. 키워드 기반 점수 계산
        for (String keyword : extractKeywords(content)) {
            // 상품문의 관련 키워드 점수는 1점만 부여
            if (Arrays.asList("상품", "펀드", "수익률", "이율", "금리", "주식", "채권").contains(keyword)) {
                typeScores.merge("상품문의", 1, Integer::sum);
            }
            // 다른 형은 2점 부여
            else if (Arrays.asList("불만", "불편", "민원", "항의", "문제").contains(keyword)) {
                typeScores.merge("불만접수", 2, Integer::sum);
            }
            else if (Arrays.asList("변경", "수정", "정보", "주소", "연락처").contains(keyword)) {
                typeScores.merge("정보변경", 2, Integer::sum);
            }
            else if (Arrays.asList("해지", "해약", "취소", "종료", "철회").contains(keyword)) {
                typeScores.merge("해지요청", 2, Integer::sum);
            }
        }
        
        // 3. 긍정적 피드백 처리
        if (content.contains("좋네요") || content.contains("좋습니다") || 
            content.contains("만족") || content.contains("감사합니다")) {
            // 상품 관련 키워드가 있더라도 일반상담으로 분류
            return "일반상담";
        }
        
        // 4. 임계값을 넘는 가장 높은 점수의 유형 반환
        return typeScores.entrySet().stream()
            .filter(e -> e.getValue() >= THRESHOLD)
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("일반상담");
    }

    /**
     * OpenAI API를 사용하여 감정 분석을 수행합니다.
     * API 키가 없거나 오류 발생 시 대체 로직��� 사용합니다.
     */
    private String analyzeSentiment(String content) {
        // OpenAiService가 초기화되지 않았으면 대체 로직 실행
        if (openAiService == null) {
            return analyzeSentimentFallback(content);
        }

        try {
            String prompt = String.format(
                "다음 상담 내용은 긍정, 부정, 중립 중 어떤 감정에 해당하나요? 답변은 '긍정', '부정', '중립' 중 하나로만 해주세요.\n\n내용: %s\n\n감정:",
                content
            );

            CompletionRequest completionRequest = CompletionRequest.builder()
                    .model("gpt-3.5-turbo-instruct")
                    .prompt(prompt)
                    .maxTokens(10)
                    .temperature(0.0)
                    .build();

            String sentiment = openAiService.createCompletion(completionRequest).getChoices().get(0).getText().trim();

            // AI의 답변이 예상과 다를 경우 기본값 처리
            if (Set.of("긍정", "부정", "중립").contains(sentiment)) {
                return sentiment;
            }
            return "중립";

        } catch (Exception e) {
            // API 호출 중 에러 발생 시 로그를 남기고 대체 로직 실행
            System.err.println("OpenAI API 호출 중 오류 발생: " + e.getMessage());
            return analyzeSentimentFallback(content);
        }
    }

    /**
     * API 호출 실패 시 대체할 간단한 감정 분석 로직
     */
    private String analyzeSentimentFallback(String content) {
        int positiveScore = 0;
        int negativeScore = 0;

        // 긍정 키워드
        List<String> positiveWords = Arrays.asList(
            "좋", "만족", "감사", "추천", "괜찮", "편리", "혜택", "성과"
        );

        // 부정 키워드
        List<String> negativeWords = Arrays.asList(
            "나쁘", "불만", "불편", "문제", "해지", "철회", "불안", "손해"
        );

        for (String keyword : extractKeywords(content)) {
            if (positiveWords.stream().anyMatch(keyword::contains)) {
                positiveScore++;
            }
            if (negativeWords.stream().anyMatch(keyword::contains)) {
                negativeScore++;
            }
        }

        if (positiveScore > negativeScore) {
            return "긍정";
        } else if (negativeScore > positiveScore) {
            return "부정";
        } else {
            return "중립";
        }
    }

    @Getter
    public static class AnalysisResult {
        private final List<String> keywords;
        private final String counselType;
        private final String sentiment;

        public AnalysisResult(List<String> keywords, String counselType, String sentiment) {
            this.keywords = keywords;
            this.counselType = counselType;
            this.sentiment = sentiment;
        }
    }
}
