package com.eggmoney.payv.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.eggmoney.payv.domain.model.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security UserDetailsService 구현체 DB에 ROLE 컬럼이 없으므로 모든 사용자에게 ROLE_USER
 * 권한 부여
 * 
 * @author 강기범
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		log.info("=== 사용자 인증 시도: email={} ===", email);

		com.eggmoney.payv.domain.model.entity.User user = userRepository.findByEmail(email).orElseThrow(() -> {
			log.warn("사용자를 찾을 수 없음: {}", email);
			return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
		});
		
		return new CustomUser(user);
	}
}
