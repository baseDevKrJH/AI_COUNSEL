package org.aitest.ai_counsel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI 상담 분석 시스템 API")
                        .version("1.0")
                        .description("상담 내용 분석 및 예측을 위한 REST API 문서"));
    }
}
