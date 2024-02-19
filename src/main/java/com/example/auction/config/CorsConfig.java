package com.example.auction.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 특정 origins 허용
        config.addAllowedOrigin("http://localhost:3000");

        // 특정 HTTP 메서드 허용
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");

        // 특정 헤더 허용
        config.addAllowedHeader("Content-Type");

        // 자격 증명 허용
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
