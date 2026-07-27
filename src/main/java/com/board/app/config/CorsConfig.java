package com.board.app.config; // ← 본인 프로젝트 패키지명으로 변경하세요

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 설정.
 * 프론트(Vercel)와 백엔드(Render)가 서로 다른 도메인이므로,
 * 백엔드가 프론트 도메인에서 오는 요청을 허용해야 한다.
 *
 * 허용 도메인은 환경변수 CORS_ALLOWED_ORIGINS 로 주입한다.
 * (Render Environment 탭에 등록. 여러 개면 콤마로 구분)
 * 예: https://board-front.vercel.app,http://localhost:3000
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
