package org.aitest.ai_counsel.service;

import lombok.RequiredArgsConstructor;
import org.aitest.ai_counsel.domain.Counsel;
import org.aitest.ai_counsel.exception.AnalysisException;
import org.aitest.ai_counsel.exception.CounselNotFoundException;
import org.aitest.ai_counsel.exception.InvalidRequestException;
import org.aitest.ai_counsel.repository.CounselRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselService {

    private final CounselRepository counselRepository;
    private final CounselAnalysisService analysisService;
    private final CounselPredictionService predictionService;

    @Transactional
    public Counsel saveCounsel(Counsel counsel) {
        return Optional.ofNullable(counsel)
                .filter(c -> Optional.ofNullable(c.getContent())
                        .filter(content -> !content.trim().isEmpty())
                        .isPresent())
                .filter(c -> Optional.ofNullable(c.getCounselorId())
                        .filter(counselorId -> !counselorId.trim().isEmpty())
                        .isPresent())
                .filter(c -> Optional.ofNullable(c.getCustomerId())
                        .filter(customerId -> !customerId.trim().isEmpty())
                        .isPresent())
                .map(c -> {
                    try {
                        return counselRepository.save(c);
                    } catch (Exception e) {
                        throw new RuntimeException("상담 정보 저장 중 오류가 발생했습니다.", e);
                    }
                })
                .orElseThrow(() -> {
                    if (counsel == null) {
                        return new InvalidRequestException("상담 정보가 누락되었습니다.");
                    } else if (counsel.getContent() == null || counsel.getContent().trim().isEmpty()) {
                        return new InvalidRequestException("상담 내용은 필수입니다.");
                    } else if (counsel.getCounselorId() == null || counsel.getCounselorId().trim().isEmpty()) {
                        return new InvalidRequestException("상담사 ID는 필수입니다.");
                    } else {
                        return new InvalidRequestException("고객 ID는 필수입니다.");
                    }
                });
    }

    public Counsel getCounselById(Long id) {
        return Optional.ofNullable(id)
                .map(counselId -> counselRepository.findById(counselId)
                        .orElseThrow(() -> new CounselNotFoundException(counselId)))
                .orElseThrow(() -> new InvalidRequestException("상��� ID가 누락되었습니다."));
    }

    public List<Counsel> getAllCounsels() {
        return counselRepository.findAll();
    }

    public List<Counsel> getCounselorHistory(String counselorId) {
        return Optional.ofNullable(counselorId)
                .filter(id -> !id.trim().isEmpty())
                .map(counselRepository::findByCounselorIdOrderByCounselDateDesc)
                .orElseThrow(() -> new InvalidRequestException("상담사 ID가 누락되었습니다."));
    }

    public List<Counsel> getCounselsByPeriod(LocalDateTime start, LocalDateTime end) {
        return Optional.ofNullable(start)
                .flatMap(startDate -> Optional.ofNullable(end)
                        .filter(endDate -> !startDate.isAfter(endDate))
                        .map(endDate -> counselRepository.findByCounselDateBetween(startDate, endDate)))
                .orElseThrow(() -> {
                    if (start == null || end == null) {
                        return new InvalidRequestException("시작일과 종료일은 필수입니다.");
                    } else {
                        return new InvalidRequestException("시작일은 종료일보다 이전이어야 합니다.");
                    }
                });
    }

    public List<Counsel> getCounselsByProduct(String productInfo) {
        return Optional.ofNullable(productInfo)
                .filter(info -> !info.trim().isEmpty())
                .map(counselRepository::findByProductInfoContaining)
                .orElseThrow(() -> new InvalidRequestException("상품 정보가 누락되었습니다."));
    }

    @Transactional
    public Counsel analyzeCounsel(Long counselId) {
        return Optional.ofNullable(counselId)
                .map(this::getCounselById)
                .map(counsel -> {
                    try {
                        CounselAnalysisService.AnalysisResult result = analysisService.analyzeCounsel(counsel);

                        String analysis = String.format("상담 유형: %s\n고객 감정: %s\n주요 키워드: %s",
                                result.getCounselType(),
                                result.getSentiment(),
                                String.join(", ", result.getKeywords()));

                        counsel.setAnalysis(analysis);
                        return counselRepository.save(counsel);
                    } catch (Exception e) {
                        throw new AnalysisException("상담 분석 처리 중 오류가 발생했습니다.", e);
                    }
                })
                .orElseThrow(() -> new InvalidRequestException("상담 ID가 누락되었습니다."));
    }

    @Transactional
    public Counsel predictNextCounselByCounselor(String counselorId) {
        return Optional.ofNullable(counselorId)
                .filter(id -> !id.trim().isEmpty())
                .map(id -> {
                    List<Counsel> history = counselRepository.findByCounselorIdOrderByCounselDateDesc(id);
                    if (history.isEmpty()) {
                        throw new InvalidRequestException("상담 이력이 없습니다.");
                    }
                    return generatePrediction(history);
                })
                .orElseThrow(() -> new InvalidRequestException("상담사 ID가 누락되었습니다."));
    }

    @Transactional
    public Counsel predictNextCounselByCustomer(String customerId) {
        return Optional.ofNullable(customerId)
                .filter(id -> !id.trim().isEmpty())
                .map(id -> {
                    List<Counsel> customerHistory = counselRepository.findByCustomerIdOrderByCounselDateDesc(id);
                    if (customerHistory.isEmpty()) {
                        throw new InvalidRequestException("해당 고객의 상담 이력이 없습니다: " + id);
                    }
                    return generatePrediction(customerHistory);
                })
                .orElseThrow(() -> new InvalidRequestException("고객 ID가 누락되었습니다."));
    }

    private Counsel generatePrediction(List<Counsel> history) {
        try {
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
        } catch (Exception e) {
            throw new AnalysisException("상담 예측 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
