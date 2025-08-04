package org.aitest.ai_counsel.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aitest.ai_counsel.domain.Counsel;
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
        if (counselHistory == null || counselHistory.isEmpty()) {
            return new PredictionResult("예측 불가", "상담 이력이 없음", Collections.emptyMap());
        }

        // 1. 상담 패턴 분석
        Map<String, Integer> typeFrequency = analyzeTypeFrequency(counselHistory);

        // 2. 주요 키워드 분석
        Map<String, Integer> keywordFrequency = analyzeKeywordFrequency(counselHistory);

        // 3. 상담 주기 분석
        double avgInterval = analyzeConsultationInterval(counselHistory);

        // 4. 예측 결과 생성
        return generatePrediction(typeFrequency, keywordFrequency, avgInterval);
    }

    /**
     * 상담 유형 빈도를 분석합니다.
     */
    private Map<String, Integer> analyzeTypeFrequency(List<Counsel> counselHistory) {
        Map<String, Integer> typeFrequency = new HashMap<>();

        for (Counsel counsel : counselHistory) {
            String analysis = counsel.getAnalysis();
            if (analysis != null && analysis.contains("상담 유형:")) {
                String type = analysis.split("\n")[0].replace("상담 유형: ", "").trim();
                typeFrequency.merge(type, 1, Integer::sum);
            }
        }

        return typeFrequency;
    }

    /**
     * 주요 키워드 빈도를 분석합니다.
     */
    private Map<String, Integer> analyzeKeywordFrequency(List<Counsel> counselHistory) {
        Map<String, Integer> keywordFrequency = new HashMap<>();

        for (Counsel counsel : counselHistory) {
            String analysis = counsel.getAnalysis();
            if (analysis != null && analysis.contains("주요 키워드:")) {
                String[] keywords = analysis.split("\n")[2]
                    .replace("주요 키워드: ", "")
                    .split(", ");

                for (String keyword : keywords) {
                    keywordFrequency.merge(keyword.trim(), 1, Integer::sum);
                }
            }
        }

        return keywordFrequency;
    }

    /**
     * 상담 간격을 분석합니다.
     */
    private double analyzeConsultationInterval(List<Counsel> counselHistory) {
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
     * 분석 결과를 바탕으로 예측 결과를 생성합니다.
     */
    private PredictionResult generatePrediction(
            Map<String, Integer> typeFrequency,
            Map<String, Integer> keywordFrequency,
            double avgInterval) {

        // 가장 빈번한 상담 유형 찾기
        String mostFrequentType = typeFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("일반상담");

        // 주요 키워드 상위 5개 추출
        Map<String, Integer> topKeywords = keywordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));

        // 예측 메시지 생성
        String predictionType = String.format("다음 상담 예상 유형: %s", mostFrequentType);
        String predictionDetail = String.format(
            "예상 상담 주기: %.1f일\n주요 예상 키워드: %s",
            avgInterval,
            String.join(", ", topKeywords.keySet())
        );

        return new PredictionResult(predictionType, predictionDetail, topKeywords);
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
