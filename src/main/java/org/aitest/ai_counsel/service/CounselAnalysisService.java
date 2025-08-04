package org.aitest.ai_counsel.service;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aitest.ai_counsel.domain.Counsel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CounselAnalysisService {

    private final Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

    private static final Map<String, String> COUNSEL_TYPES = Map.of(
        "상품문의", "상품의 특성, 가격, 조건 등에 대한 문의",
        "불만접수", "서비스나 상품에 대한 불만 제기",
        "정보변경", "고객 정보 변경 요청",
        "해지요청", "서비스 해지나 계약 종료 요청",
        "일반상담", "기타 일반적인 문의사항"
    );

    /**
     * 상담 내용을 분석하여 결과를 반환합니다.
     */
    public AnalysisResult analyzeCounsel(Counsel counsel) {
        String content = counsel.getContent();

        // 형태소 분석을 통한 키워드 추출
        List<String> keywords = extractKeywords(content);

        // 상담 유형 분류
        String counselType = classifyCounselType(content, keywords);

        // 감정 분석
        String sentiment = analyzeSentiment(content, keywords);

        return new AnalysisResult(keywords, counselType, sentiment);
    }

    /**
     * 키워드를 기반으로 상담 유형을 분류합니다.
     */
    private String classifyCounselType(String content, List<String> keywords) {
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
        for (String keyword : keywords) {
            // 상품문의 관련 키워드 점수는 1점만 부여
            if (Arrays.asList("상품", "펀드", "수익률", "이율", "금리", "주식", "채권").contains(keyword)) {
                typeScores.merge("상품문의", 1, Integer::sum);
            }
            // 다른 ��형은 2점 부여
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
     * KOMORAN 형태소 분석기를 사용하여 주요 키워드를 추출합니다.
     */
    private List<String> extractKeywords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return Collections.emptyList();
        }

        KomoranResult analyzeResult = komoran.analyze(content);
        Set<String> keywords = new HashSet<>();
        
        // 1. 기본 명사 추출
        List<String> nouns = analyzeResult.getNouns();
        
        // 2. 복합 명사 처리
        for (String noun : nouns) {
            // 불용어 필터링
            if (noun.length() <= 1 || Arrays.asList("것", "수", "등", "점", "분", "글", "말", "때", "내", "중").contains(noun)) {
                continue;
            }
            
            // 복합 명사 처리
            if (noun.equals("주식형펀드")) {
                keywords.add("주식형");
                keywords.add("펀드");
            } else if (noun.endsWith("형") && noun.length() > 2) {
                keywords.add(noun); // "주식형" 전체를 키워드로 추가
            } else if (!noun.equals("형")) { // "형"이라는 단어 자체는 제외
                keywords.add(noun);
            }
        }
        
        // 3. 특수 패턴 처리
        if (content.contains("주식형 펀드")) {
            keywords.add("주식형");
            keywords.add("펀드");
        }
        
        return new ArrayList<>(keywords);
    }

    /**
     * 키워드를 기반으로 간단한 감정 분석을 수행합니다.
     */
    private String analyzeSentiment(String content, List<String> keywords) {
        int positiveScore = 0;
        int negativeScore = 0;

        // 긍정 키워드
        List<String> positiveWords = Arrays.asList(
            "좋", "만족", "감사", "추천", "괜찮", "편리", "혜택", "성과"
        );

        // 부정 키워드
        List<String> negativeWords = Arrays.asList(
            "나쁘", "불만", "불편", "문제", "해지", "철회", "불안", "손실"
        );

        for (String keyword : keywords) {
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
