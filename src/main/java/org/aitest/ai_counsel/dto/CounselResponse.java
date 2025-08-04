package org.aitest.ai_counsel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.aitest.ai_counsel.domain.Counsel;

import java.time.LocalDateTime;

@Getter
@Builder
public class CounselResponse {
    @Schema(description = "상담 ID")
    private Long id;

    @Schema(description = "상담사 ID")
    private String counselorId;

    @Schema(description = "상담 내용")
    private String content;

    @Schema(description = "상담 일시")
    private LocalDateTime counselDate;

    @Schema(description = "상품 정보")
    private String productInfo;

    @Schema(description = "분석 결과")
    private String analysis;

    @Schema(description = "예측 정보")
    private String prediction;

    public static CounselResponse from(Counsel counsel) {
        return CounselResponse.builder()
                .id(counsel.getId())
                .counselorId(counsel.getCounselorId())
                .content(counsel.getContent())
                .counselDate(counsel.getCounselDate())
                .productInfo(counsel.getProductInfo())
                .analysis(counsel.getAnalysis())
                .prediction(counsel.getPrediction())
                .build();
    }
}
