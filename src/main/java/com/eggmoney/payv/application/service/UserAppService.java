package com.eggmoney.payv.application.service;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.model.repository.UserRepository;
import com.eggmoney.payv.domain.shared.error.DomainException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 관련 애플리케이션 서비스 회원가입, 로그인, 개인정보 관리 등의 기능을 제공
 * @author 강기범
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAppService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// 이메일 정규식 패턴
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	
	// 테이블 제약에 맞춘 길이 제한
	private static final int MIN_PASSWORD_LENGTH = 8;
	private static final int MAX_EMAIL_LENGTH = 50; // VARCHAR2(50)에 맞춤
	private static final int MAX_NAME_LENGTH = 20; // VARCHAR2(20 CHAR)에 맞춤
	private static final int MAX_PASSWORD_LENGTH = 100;
	
	
	/**
	 * 회원가입
	 */
	@Transactional
	public User register(String email, String name, String rawPassword) {

		validateEmailFormat(email);
		validatePasswordStrength(rawPassword);
		validateName(name);

		if (userRepository.findByEmail(email).isPresent()) {
			log.warn("이메일 중복: {}", email);
			throw new DomainException("이미 사용중인 이메일입니다: " + email);
		}

		String encodedPassword = passwordEncoder.encode(rawPassword);
		User user = User.create(email, encodedPassword, name);
		userRepository.save(user);

		return user;
	}

	/**
	 * 로그인 인증
	 */
	@Transactional(readOnly = true)
	public User authenticate(String email, String rawPassword) {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("이메일은 필수입니다.");
		}
		if (rawPassword == null || rawPassword.trim().isEmpty()) {
			throw new IllegalArgumentException("비밀번호는 필수입니다.");
		}

		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			log.warn("존재하지 않는 사용자: {}", email);
			return new DomainException("이메일 또는 비밀번호가 올바르지 않습니다.");
		});

		if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
			log.warn("비밀번호 불일치: email={}", email);
			throw new DomainException("이메일 또는 비밀번호가 올바르지 않습니다.");
		}

		return user;
	}
	
	
    
    
    
    
    
    // ========== 검증 메서드들 ==========
 	private void validateEmailFormat(String email) {
 		if (email == null || email.trim().isEmpty()) {
 			throw new IllegalArgumentException("이메일은 필수입니다.");
 		}

 		String trimmedEmail = email.trim();
 		if (trimmedEmail.length() > MAX_EMAIL_LENGTH) {
 			throw new IllegalArgumentException("이메일은 " + MAX_EMAIL_LENGTH + "자를 초과할 수 없습니다.");
 		}

 		if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
 			throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
 		}
 	}

 	private void validatePasswordStrength(String password) {
 		if (password == null || password.trim().isEmpty()) {
 			throw new IllegalArgumentException("비밀번호는 필수입니다.");
 		}

 		if (password.length() < MIN_PASSWORD_LENGTH) {
 			throw new IllegalArgumentException("비밀번호는 최소 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
 		}

 		if (password.length() > MAX_PASSWORD_LENGTH) {
 			throw new IllegalArgumentException("비밀번호는 " + MAX_PASSWORD_LENGTH + "자를 초과할 수 없습니다.");
 		}
 	}

 	private void validateName(String name) {
 		if (name == null || name.trim().isEmpty()) {
 			throw new IllegalArgumentException("이름은 필수입니다.");
 		}

 		if (name.trim().length() > MAX_NAME_LENGTH) {
 			throw new IllegalArgumentException("이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다.");
 		}
 	}
}
