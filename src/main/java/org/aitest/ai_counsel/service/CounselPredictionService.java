package org.aitest.ai_counsel.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aitest.ai_counsel.domain.Counsel;
import org.aitest.ai_counsel.exception.AnalysisException;
import org.aitest.ai_counsel.exception.InvalidRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselPredictionService {

    private final CounselAnalysisService analysisService;

    /**
     * 과거 상담 내역을 기반으로 다음 상담을 예측합니다.
     */
    public PredictionResult predictNextCounsel(List<Counsel> counselHistory) {
        return Optional.ofNullable(counselHistory)
                .filter(history -> !history.isEmpty())
                .map(history -> {
                    try {
                        // 1. 상담 패턴 분석
                        Map<String, Integer> typeFrequency = analyzeTypeFrequency(history);

                        // 2. 주요 키워드 분석
                        Map<String, Integer> keywordFrequency = analyzeKeywordFrequency(history);

                        // 3. 상담 주기 분석
                        double averageCycle = calculateAverageCycle(history);

                        // 4. 예측 결과 생성
                        String predictedType = predictMostLikelyType(typeFrequency);
                        String details = generatePredictionDetails(typeFrequency, averageCycle, history.size());

                        // 상위 5개 키워드만 반환
                        Map<String, Integer> topKeywords = keywordFrequency.entrySet().stream()
                                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                .limit(5)
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (e1, e2) -> e1,
                                        LinkedHashMap::new
                                ));

                        return new PredictionResult(predictedType, details, topKeywords);
                    } catch (Exception e) {
                        throw new AnalysisException("상담 예측 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
                    }
                })
                .orElseThrow(() -> new InvalidRequestException("예측을 위한 상담 이력이 없습니다."));
    }

    /**
     * 상담 유형 빈도를 분석합니다.
     */
    private Map<String, Integer> analyzeTypeFrequency(List<Counsel> counselHistory) {
        Map<String, Integer> typeFrequency = new HashMap<>();

        counselHistory.forEach(counsel ->
            Optional.ofNullable(counsel.getAnalysis())
                .filter(analysis -> analysis.contains("상담 유형:"))
                .map(analysis -> analysis.split("\n")[0].replace("상담 유형: ", "").trim())
                .ifPresent(type -> typeFrequency.merge(type, 1, Integer::sum))
        );

        return typeFrequency;
    }

    /**
     * 주요 키워드 빈도를 분석합니다.
     */
    private Map<String, Integer> analyzeKeywordFrequency(List<Counsel> counselHistory) {
        Map<String, Integer> keywordFrequency = new HashMap<>();

        counselHistory.forEach(counsel ->
            Optional.ofNullable(counsel.getAnalysis())
                .filter(analysis -> analysis.contains("주요 키워드:"))
                .map(analysis -> analysis.split("\n")[2].replace("주요 키워드: ", "").split(", "))
                .ifPresent(keywords -> {
                    for (String keyword : keywords) {
                        keywordFrequency.merge(keyword.trim(), 1, Integer::sum);
                    }
                })
        );

        return keywordFrequency;
    }

    /**
     * 상담 간격을 분석합니다.
     */
    private double calculateAverageCycle(List<Counsel> counselHistory) {
        if (counselHistory.size() < 2) {
            return 0.0;
        }

        long totalDays = 0;
        for (int i = 0; i < counselHistory.size() - 1; i++) {
            LocalDateTime current = counselHistory.get(i).getCounselDate();
            LocalDateTime next = counselHistory.get(i + 1).getCounselDate();
            totalDays += Math.abs(current.until(next, java.time.temporal.ChronoUnit.DAYS));
        }

        return (double) totalDays / (counselHistory.size() - 1);
    }

    /**
     * 가장 가능성이 높은 상담 유형을 예측합니다.
     */
    private String predictMostLikelyType(Map<String, Integer> typeFrequency) {
        return typeFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("일반상담");
    }

    /**
     * 예측 세부 정보를 생성합니다.
     */
    private String generatePredictionDetails(Map<String, Integer> typeFrequency, double averageCycle, int historySize) {
        StringBuilder details = new StringBuilder();
        details.append(String.format("상담 주기: %.1f일\n", averageCycle));

        // 상담 유형에 따른 세부 정보 추가
        for (Map.Entry<String, Integer> entry : typeFrequency.entrySet()) {
            String type = entry.getKey();
            Integer count = entry.getValue();
            details.append(String.format(" - %s: %d회\n", type, count));
        }

        return details.toString();
    }

    @Getter
    public static class PredictionResult {
        private final String predictedType;
        private final String details;
        private final Map<String, Integer> topKeywords;

        public PredictionResult(String predictedType, String details, Map<String, Integer> topKeywords) {
            this.predictedType = predictedType;
            this.details = details;
            this.topKeywords = topKeywords;
        }
    }
}
