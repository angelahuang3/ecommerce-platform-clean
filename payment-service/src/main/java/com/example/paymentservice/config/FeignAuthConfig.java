package com.example.paymentservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthConfig {

    private static final String AUTH = "Authorization";

    @Bean
    public RequestInterceptor bearerTokenRelayInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1) 先嘗試從當前 HTTP Request header 取得 Authorization
                String tokenFromHeader = resolveAuthHeaderFromRequest();

                // 2) 若沒有，嘗試從 SecurityContextHolder 取（e.g. JwtAuthenticationToken）
                if (tokenFromHeader == null) {
                    tokenFromHeader = resolveAuthHeaderFromSecurityContext();
                }

                // 3) 若還是沒有，可以選擇不加（讓下游以匿名拒絕），
                //    或者用服務間固定 token（可用環境變數 SERVICE_TOKEN）
                if (tokenFromHeader == null) {
                    String serviceToken = System.getenv("SERVICE_TOKEN");
                    if (serviceToken != null && !serviceToken.isBlank()) {
                        tokenFromHeader = serviceToken.startsWith("Bearer ")
                                ? serviceToken
                                : "Bearer " + serviceToken;
                    }
                }

                if (tokenFromHeader != null) {
                    template.header(AUTH, tokenFromHeader);
                }
            }
        };
    }

    private String resolveAuthHeaderFromRequest() {
        try {
            RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            if (ra instanceof ServletRequestAttributes attrs) {
                HttpServletRequest req = attrs.getRequest();
                String h = req.getHeader(AUTH);
                if (h != null && !h.isBlank()) {
                    // 確保有 Bearer 前綴（前端已帶就是完整字串，不要重複疊）
                    return h.startsWith("Bearer ") ? h : "Bearer " + h;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String resolveAuthHeaderFromSecurityContext() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return null;

            // 常見情形 1：自訂的 JwtAuthentication 裡面直接存原始 token（自行調整）
            Object credentials = auth.getCredentials();
            if (credentials instanceof String raw && !raw.isBlank()) {
                return raw.startsWith("Bearer ") ? raw : "Bearer " + raw;
            }

            // 常見情形 2：使用 spring-security-oauth2-resource-server 的 JwtAuthenticationToken
            // 需要依你的專案實際型別轉型抓 tokenValue（若沒有用就忽略）
            // if (auth instanceof JwtAuthenticationToken jwtAuth) {
            //     return "Bearer " + jwtAuth.getToken().getTokenValue();
            // }

        } catch (Exception ignored) {}
        return null;
    }
}