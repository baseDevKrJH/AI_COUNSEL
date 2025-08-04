package org.aitest.ai_counsel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.aitest.ai_counsel.domain.Counsel;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "상담 응답")
public class CounselResponse {

    @Schema(description = "상담 ID", example = "1")
    private Long id;

    @Schema(description = "상담사 ID", example = "counselor001")
    private String counselorId;

    @Schema(description = "고객 ID", example = "customer001")
    private String customerId;

    @Schema(description = "상담 내용", example = "펀드 상품에 대해 문의드리고 싶습니다.")
    private String content;

    @Schema(description = "상담 일시", example = "2024-01-15T09:30:00")
    private LocalDateTime counselDate;

    @Schema(description = "상품 정보", example = "주식형 펀드")
    private String productInfo;

    @Schema(description = "분석 결과", example = "상담 유형: 상품문의\n고객 감정: 중립\n주요 키워드: 펀드, 상품, 수익률")
    private String analysis;

    @Schema(description = "예측 결과", example = "일반상담\n상담 주기: 30일\n - 상품문의: 2회\n - 일반상담: 1회")
    private String prediction;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시")
    private LocalDateTime modifiedAt;

    public static CounselResponse from(Counsel counsel) {
        return CounselResponse.builder()
                .id(counsel.getId())
                .counselorId(counsel.getCounselorId())
                .customerId(counsel.getCustomerId())
                .content(counsel.getContent())
                .counselDate(counsel.getCounselDate())
                .productInfo(counsel.getProductInfo())
                .analysis(counsel.getAnalysis())
                .prediction(counsel.getPrediction())
                .createdAt(counsel.getCreatedAt())
                .modifiedAt(counsel.getModifiedAt())
                .build();
    }
}
