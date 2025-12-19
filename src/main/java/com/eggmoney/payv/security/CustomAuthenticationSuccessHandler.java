package com.eggmoney.payv.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 성공 후 처리 핸들러
 */
@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		log.info("로그인 성공: {}", authentication.getName());
		
		// Context Path를 포함한 절대 경로로 리다이렉트
		String contextPath = request.getContextPath();
		String redirectUrl = contextPath + "/ledgers";
		
		log.info("로그인 성공 리다이렉트: {}", redirectUrl);
		response.sendRedirect(redirectUrl);
	}
}
