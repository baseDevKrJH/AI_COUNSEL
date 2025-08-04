package org.aitest.ai_counsel.service;

import lombok.RequiredArgsConstructor;
import org.aitest.ai_counsel.domain.Counsel;
import org.aitest.ai_counsel.repository.CounselRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselService {

    private final CounselRepository counselRepository;
    private final CounselAnalysisService analysisService;
    private final CounselPredictionService predictionService;

    @Transactional
    public Counsel saveCounsel(Counsel counsel) {
        // TODO: 상담 내용 분석 로직 추가
        // TODO: 다음 상담 예측 로직 추가
        return counselRepository.save(counsel);
    }

    public List<Counsel> getCounselorHistory(String counselorId) {
        return counselRepository.findByCounselorIdOrderByCounselDateDesc(counselorId);
    }

    public List<Counsel> getCounselsByPeriod(LocalDateTime start, LocalDateTime end) {
        return counselRepository.findByCounselDateBetween(start, end);
    }

    public List<Counsel> getCounselsByProduct(String productInfo) {
        return counselRepository.findByProductInfoContaining(productInfo);
    }

    @Transactional
    public Counsel analyzeCounsel(Long counselId) {
        Counsel counsel = counselRepository.findById(counselId)
                .orElseThrow(() -> new RuntimeException("상담 내역을 찾을 수 없습니다."));

        CounselAnalysisService.AnalysisResult result = analysisService.analyzeCounsel(counsel);

        StringBuilder analysis = new StringBuilder();
        analysis.append("상담 유형: ").append(result.getCounselType()).append("\n");
        analysis.append("고객 감정: ").append(result.getSentiment()).append("\n");
        analysis.append("주요 키워드: ").append(String.join(", ", result.getKeywords()));

        counsel.setAnalysis(analysis.toString());
        return counselRepository.save(counsel);
    }

    @Transactional
    public Counsel predictNextCounsel(String counselorId) {
        List<Counsel> history = getCounselorHistory(counselorId);
        if (history.isEmpty()) {
            throw new RuntimeException("상담 이력이 없습니다.");
        }

        Counsel latestCounsel = history.get(0);
        CounselPredictionService.PredictionResult prediction = predictionService.predictNextCounsel(history);

        // 예측 결과를 문자열로 변환하여 저장
        StringBuilder predictionText = new StringBuilder();
        predictionText.append(prediction.getPredictedType()).append("\n");
        predictionText.append(prediction.getDetails()).append("\n");
        predictionText.append("키워드 빈도:\n");
        prediction.getTopKeywords().forEach((keyword, frequency) ->
            predictionText.append(String.format("- %s (%d회)\n", keyword, frequency))
        );

        latestCounsel.setPrediction(predictionText.toString());
        return counselRepository.save(latestCounsel);
    }
}
