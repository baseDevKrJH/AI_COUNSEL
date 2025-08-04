package org.aitest.ai_counsel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CounselRequest {
    @Schema(description = "상담사 ID", example = "COUN001")
    private String counselorId;

    @Schema(description = "상담 내용", example = "고객이 펀드 상품에 대해 문의하였음...")
    private String content;

    @Schema(description = "상담 일시")
    private LocalDateTime counselDate;

    @Schema(description = "상품 정보", example = "주식형펀드")
    private String productInfo;
}
