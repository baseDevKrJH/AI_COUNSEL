package org.aitest.ai_counsel.service;

import org.aitest.ai_counsel.domain.Counsel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CounselPredictionServiceTest {

    @Autowired
    private CounselPredictionService predictionService;

    private List<Counsel> testHistory;

    @BeforeEach
    void setUp() {
        // 테스트용 상담 이력 생성
        Counsel counsel1 = createTestCounsel(
            "COUNSEL001",
            "주식형 펀드 수익률 문의",
            LocalDateTime.now().minusDays(30),
            "상담 유형: 상품문의\n고객 감정: 중립\n주요 키워드: 주식형, 펀드, 수익률"
        );

        Counsel counsel2 = createTestCounsel(
            "COUNSEL001",
            "펀드 수수료 관련 문의",
            LocalDateTime.now().minusDays(20),
            "상담 유형: 상품문의\n고객 감정: 중립\n주요 키워드: 펀드, 수수료"
        );

        Counsel counsel3 = createTestCounsel(
            "COUNSEL001",
            "펀드 해지 요청",
            LocalDateTime.now().minusDays(10),
            "상담 유형: 해지요청\n고객 감정: 부정\n주요 키워드: 펀드, 해지"
        );

        testHistory = Arrays.asList(counsel3, counsel2, counsel1); // 최신순
    }

    private Counsel createTestCounsel(String counselorId, String content, LocalDateTime counselDate, String analysis) {
        Counsel counsel = new Counsel();
        counsel.setCounselorId(counselorId);
        counsel.setContent(content);
        counsel.setCounselDate(counselDate);
        counsel.setAnalysis(analysis);
        return counsel;
    }

    @Test
    void testPredictNextCounsel() {
        // when
        CounselPredictionService.PredictionResult result = predictionService.predictNextCounsel(testHistory);

        // then
        assertNotNull(result);
        assertNotNull(result.getPredictedType());
        assertNotNull(result.getDetails());
        assertNotNull(result.getTopKeywords());

        // 상담 유형 예측 검증
        assertTrue(result.getPredictedType().contains("상품문의") ||
                  result.getPredictedType().contains("해지요청"));

        // 상담 주기 검증
        assertTrue(result.getDetails().contains("예상 상담 주기: 10.0일"));

        // 키워드 빈도 검증
        Map<String, Integer> keywords = result.getTopKeywords();
        assertTrue(keywords.containsKey("펀드"));
        assertEquals(3, keywords.get("펀드")); // 펀드 키워드는 3번 등장
    }

    @Test
    void testPredictWithEmptyHistory() {
        // when
        CounselPredictionService.PredictionResult result = predictionService.predictNextCounsel(List.of());

        // then
        assertEquals("예측 불가", result.getPredictedType());
        assertEquals("상담 이력이 없음", result.getDetails());
        assertTrue(result.getTopKeywords().isEmpty());
    }

    @Test
    void testPredictWithSingleCounsel() {
        // given
        List<Counsel> singleHistory = List.of(testHistory.get(0));

        // when
        CounselPredictionService.PredictionResult result = predictionService.predictNextCounsel(singleHistory);

        // then
        assertNotNull(result);
        assertTrue(result.getPredictedType().contains("해지요청"));
        assertEquals(0.0, Double.parseDouble(result.getDetails()
            .split("\n")[0]
            .replace("예상 상담 주기: ", "")
            .replace("일", "")
            .trim()));
    }
}
