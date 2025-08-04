package org.aitest.ai_counsel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "상담 등록 요청")
public class CounselRequest {

    @Schema(description = "상담사 ID", example = "counselor001")
    @NotBlank(message = "상담사 ID는 필수입니다.")
    @Size(max = 50, message = "상담사 ID는 50자를 초과할 수 없습니다.")
    private String counselorId;

    @Schema(description = "고객 ID", example = "customer001")
    @NotBlank(message = "고객 ID는 필수입니다.")
    @Size(max = 50, message = "고객 ID는 50자를 초과할 수 없습니다.")
    private String customerId;

    @Schema(description = "상담 내용", example = "펀드 상품에 대해 문의드리고 싶습니다.")
    @NotBlank(message = "상담 내용은 필수입니다.")
    @Size(min = 10, max = 2000, message = "상담 내용은 10자 이상 2000자 이하로 입력해주세요.")
    private String content;

    @Schema(description = "상담 일시", example = "2024-01-15T09:30:00")
    private LocalDateTime counselDate;

    @Schema(description = "상품 정보", example = "주식형 펀드")
    @Size(max = 200, message = "상품 정보는 200자를 초과할 수 없습니다.")
    private String productInfo;
}
