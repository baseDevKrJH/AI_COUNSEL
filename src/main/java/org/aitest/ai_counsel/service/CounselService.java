package org.aitest.ai_counsel.service;

import lombok.RequiredArgsConstructor;
import org.aitest.ai_counsel.domain.Counsel;
import org.aitest.ai_counsel.exception.CounselNotFoundException;
import org.aitest.ai_counsel.exception.ErrorCode;
import org.aitest.ai_counsel.exception.InvalidRequestException;
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
        if (counsel == null) {
            throw new InvalidRequestException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return counselRepository.save(counsel);
    }

    public Counsel getCounselById(Long id) {
        return counselRepository.findById(id)
                .orElseThrow(() -> new CounselNotFoundException(ErrorCode.COUNSEL_NOT_FOUND));
    }

    public List<Counsel> getAllCounsels() {
        return counselRepository.findAll();
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
        Counsel counsel = getCounselById(counselId);
        CounselAnalysisService.AnalysisResult result = analysisService.analyzeCounsel(counsel);

        String analysis = String.format("상담 유형: %s\n고객 감정: %s\n주요 키워드: %s",
                result.getCounselType(),
                result.getSentiment(),
                String.join(", ", result.getKeywords()));

        counsel.setAnalysis(analysis);
        return counselRepository.save(counsel);
    }

    @Transactional
    public Counsel predictNextCounselByCounselor(String counselorId) {
        List<Counsel> history = counselRepository.findByCounselorIdOrderByCounselDateDesc(counselorId);
        if (history.isEmpty()) {
            throw new InvalidRequestException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return generatePrediction(history);
    }

    @Transactional
    public Counsel predictNextCounselByCustomer(String customerId) {
        List<Counsel> customerHistory = counselRepository.findByCustomerIdOrderByCounselDateDesc(customerId);
        if (customerHistory.isEmpty()) {
            throw new InvalidRequestException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return generatePrediction(customerHistory);
    }

    private Counsel generatePrediction(List<Counsel> history) {
        Counsel latestCounsel = history.get(0);
        CounselPredictionService.PredictionResult prediction = predictionService.predictNextCounsel(history);

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