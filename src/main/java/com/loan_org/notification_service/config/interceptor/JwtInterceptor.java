package com.loan_org.notification_service.config.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Value("${jwt.signing_key}")
    private String signingKey;

    private SecretKey secretKey;

    private static final String AUTH_HEADER_MISSING_MESSAGE = """
            {
                "error": "UNAUTHORIZED",
                "status": 401,
                "message": "No Authorization header was provided"
            }
            """;

    private static final String AUTH_HEADER_NOT_BEARER_TOKEN_MESSAGE = """
            {
                "error": "UNAUTHORIZED",
                "status": 401,
                "message": "No Bearer token was provided in Authorization header. Please provide as `Bearer <access-token>`"
            }
            """;

    private static final String UNAUTHORIZED_MESSAGE_TOKEN_EXPIRED = """
            {
                "error": "UNAUTHORIZED",
                "status": 401,
                "message": "The provided Bearer token is expired. Please issue a new access token from IAM service."
            }
            """;

    private static final String UNAUTHORIZED_MESSAGE_TOKEN_INVALID = """
            {
                "error": "UNAUTHORIZED",
                "status": 401,
                "message": "The provided Bearer token is invalid. Please issue a new access token from IAM service."
            }
            """;

    private static final String RESPONSE_MEDIA_TYPE = "application/json";
    private static final String RESPONSE_ENCODING = "UTF-8";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    @PostConstruct
    private void init() {
        secretKey = Keys.hmacShaKeyFor(
                signingKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        response.setContentType(RESPONSE_MEDIA_TYPE);
        response.setCharacterEncoding(RESPONSE_ENCODING);

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || authHeader.isBlank()) {
            return unAuthorizedResponse(response, AUTH_HEADER_MISSING_MESSAGE);
        }

        if(!authHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            return unAuthorizedResponse(response, AUTH_HEADER_NOT_BEARER_TOKEN_MESSAGE);
        }

        try {
            String token  = authHeader.substring(BEARER_TOKEN_PREFIX.length());

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            request.setAttribute("userId", claims.getSubject());

            @SuppressWarnings("unchecked")  // Intellij loves this for god's sake
            Map<String, Object> attributes =
                    (Map<String, Object>) claims.getOrDefault("attributes", Map.of());

            request.setAttribute("attributes", attributes);
            return true;
        } catch (ExpiredJwtException _) {
            return unAuthorizedResponse(response, UNAUTHORIZED_MESSAGE_TOKEN_EXPIRED);
        } catch (JwtException _) {
            return unAuthorizedResponse(response, UNAUTHORIZED_MESSAGE_TOKEN_INVALID);
        }
    }

    private boolean unAuthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(message);
        response.getWriter().flush();
        return false;
    }
}