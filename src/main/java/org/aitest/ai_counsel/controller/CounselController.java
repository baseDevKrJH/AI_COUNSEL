package org.aitest.ai_counsel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.aitest.ai_counsel.domain.Counsel;
import org.aitest.ai_counsel.dto.CounselRequest;
import org.aitest.ai_counsel.dto.CounselResponse;
import org.aitest.ai_counsel.service.CounselService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "상담 관리", description = "상담 내용 관리 및 분석 API")
@RestController
@RequestMapping("/api/counsels")
@RequiredArgsConstructor
public class CounselController {

    private final CounselService counselService;

    @Operation(summary = "상담 내용 저장", description = "새로운 상담 내용을 저장합니다.")
    @PostMapping
    public ResponseEntity<CounselResponse> createCounsel(@RequestBody CounselRequest request) {
        Counsel counsel = new Counsel();
        counsel.setCounselorId(request.getCounselorId());
        counsel.setContent(request.getContent());
        counsel.setCounselDate(request.getCounselDate());
        counsel.setProductInfo(request.getProductInfo());

        Counsel saved = counselService.saveCounsel(counsel);
        return ResponseEntity.ok(CounselResponse.from(saved));
    }

    @Operation(summary = "상담사별 상담 내역 조회", description = "특정 상담사의 상담 내역을 조회합니다.")
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<List<CounselResponse>> getCounselorHistory(
            @Parameter(description = "상담사 ID") @PathVariable String counselorId) {
        List<CounselResponse> responses = counselService.getCounselorHistory(counselorId)
                .stream()
                .map(CounselResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "기간별 상담 내역 조회", description = "지정된 기간의 상담 내역을 조회합니다.")
    @GetMapping("/period")
    public ResponseEntity<List<CounselResponse>> getCounselsByPeriod(
            @Parameter(description = "시작 일시")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "종료 일시")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<CounselResponse> responses = counselService.getCounselsByPeriod(start, end)
                .stream()
                .map(CounselResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "상품별 상담 내역 조회", description = "특정 상품과 관련된 상담 내역을 조회합니다.")
    @GetMapping("/product")
    public ResponseEntity<List<CounselResponse>> getCounselsByProduct(
            @Parameter(description = "상품 정보") @RequestParam String productInfo) {
        List<CounselResponse> responses = counselService.getCounselsByProduct(productInfo)
                .stream()
                .map(CounselResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "상담 내용 분석", description = "특정 상담 내용을 분석합니다.")
    @PostMapping("/{counselId}/analyze")
    public ResponseEntity<CounselResponse> analyzeCounsel(
            @Parameter(description = "상담 ID") @PathVariable Long counselId) {
        Counsel analyzed = counselService.analyzeCounsel(counselId);
        return ResponseEntity.ok(CounselResponse.from(analyzed));
    }

    @Operation(summary = "다음 상담 예측", description = "상담사의 다음 상담 내용을 예측합니다.")
    @PostMapping("/counselor/{counselorId}/predict")
    public ResponseEntity<CounselResponse> predictNextCounsel(
            @Parameter(description = "상담사 ID") @PathVariable String counselorId) {
        Counsel predicted = counselService.predictNextCounsel(counselorId);
        if (predicted == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(CounselResponse.from(predicted));
    }
}
