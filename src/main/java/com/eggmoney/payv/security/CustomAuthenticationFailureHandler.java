package com.eggmoney.payv.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 실패 후 처리 핸들러
 */
@Component
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        
        log.warn("로그인 실패: {}", exception.getMessage());
        
        // Context Path를 포함한 절대 경로로 리다이렉트
        String contextPath = request.getContextPath();
        String redirectUrl = contextPath + "/login?error=true";
        
        log.info("로그인 실패 리다이렉트: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
