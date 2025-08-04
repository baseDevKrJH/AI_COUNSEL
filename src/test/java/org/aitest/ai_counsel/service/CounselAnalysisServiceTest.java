package org.aitest.ai_counsel.service;

import org.aitest.ai_counsel.domain.Counsel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CounselAnalysisServiceTest {

    @Autowired
    private CounselAnalysisService analysisService;

    private Counsel testCounsel;

    @BeforeEach
    void setUp() {
        testCounsel = new Counsel();
        testCounsel.setCounselorId("COUNSEL001");
        testCounsel.setCounselDate(LocalDateTime.now());
    }

    @Test
    void testKeywordExtraction_ProductInquiry() {
        // given
        testCounsel.setContent("주식형 펀드 상품에 대해 문의드립니다. 최근 수익률은 어떻게 되나요?");
        testCounsel.setProductInfo("주식형펀드");

        // when
        CounselAnalysisService.AnalysisResult result = analysisService.analyzeCounsel(testCounsel);

        // then
        List<String> keywords = result.getKeywords();
        assertNotNull(keywords);
        assertFalse(keywords.isEmpty());
        assertTrue(keywords.contains("주식형"));
        assertTrue(keywords.contains("펀드"));
        assertTrue(keywords.contains("수익률"));
    }

    @Test
    void testKeywordExtraction_ComplaintHandling() {
        // given
        testCounsel.setContent("펀드 수수료가 너무 비싸네요. 다른 상품으로 변경하고 싶습니다.");
        testCounsel.setProductInfo("펀드");

        // when
        CounselAnalysisService.AnalysisResult result = analysisService.analyzeCounsel(testCounsel);

        // then
        List<String> keywords = result.getKeywords();
        assertNotNull(keywords);
        assertFalse(keywords.isEmpty());
        assertTrue(keywords.contains("펀드"));
        assertTrue(keywords.contains("수수료"));
        assertTrue(keywords.contains("변경"));
    }

    @Test
    void testKeywordExtraction_EmptyContent() {
        // given
        testCounsel.setContent("");

        // when
        CounselAnalysisService.AnalysisResult result = analysisService.analyzeCounsel(testCounsel);

        // then
        List<String> keywords = result.getKeywords();
        assertNotNull(keywords);
        assertTrue(keywords.isEmpty());
    }

    @Test
    void testDefaultValuesWithoutGPT() {
        // given
        testCounsel.setContent("주식형 펀드 수익률이 좋네요.");

        // when
        CounselAnalysisService.AnalysisResult result = analysisService.analyzeCounsel(testCounsel);

        // then
        // API 키가 없을 때의 기본값 확인
        assertEquals("일반상담", result.getCounselType());
        assertEquals("중립", result.getSentiment());

        // 키워드 추출은 정상 동작해야 함
        List<String> keywords = result.getKeywords();
        assertNotNull(keywords);
        assertFalse(keywords.isEmpty());
        assertTrue(keywords.contains("펀드"));
        assertTrue(keywords.contains("수익률"));
    }
}
