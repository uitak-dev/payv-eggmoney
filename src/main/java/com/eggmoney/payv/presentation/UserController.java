package com.eggmoney.payv.presentation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eggmoney.payv.application.service.UserAppService;
import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.presentation.dto.SignupForm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 인증 및 프로필 관리 컨트롤러 회원가입, 로그인, 로그아웃, 마이페이지, 프로필 수정, 비밀번호 변경
 * @author 강기범
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class UserController {

	private final UserAppService userAppService;
	
	/**
	 * 로그인 페이지 표시
	 */
	@GetMapping("/login")
	public String loginForm(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout,
			@RequestParam(value = "expired", required = false) String expired,
			@RequestParam(value = "signup", required = false) String signup, Model model) {

		log.info("로그인 페이지 요청 - error: {}, logout: {}, expired: {}, signup: {}", error, logout, expired, signup);

		if (error != null) {
			model.addAttribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다.");
		}

		if (logout != null) {
			model.addAttribute("logoutMessage", "성공적으로 로그아웃되었습니다.");
		}

		if (expired != null) {
			model.addAttribute("expiredMessage", "세션이 만료되었습니다. 다시 로그인해주세요.");
		}

		if (signup != null) {
			model.addAttribute("signupMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
		}

		return "user/login";
	}

	/**
	 * 회원가입 페이지 표시
	 */
	@GetMapping("/signup")
	public String signupForm(Model model) {
		log.info("회원가입 페이지 요청");
		model.addAttribute("signupForm", new SignupForm());
		return "user/signup";
	}

	/**
	 * 회원가입 처리
	 */
	@PostMapping("/signup")
	public String signup(@Valid @ModelAttribute SignupForm signupForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, Model model) {

		log.info("회원가입 처리 요청: email={}, name={}", signupForm.getEmail(), signupForm.getName());

	    // 비밀번호 확인 검사 (기본 유효성 검사와 함께 처리)
	    if (!bindingResult.hasFieldErrors("password") && 
	        !bindingResult.hasFieldErrors("confirmPassword") &&
	        !signupForm.getPassword().equals(signupForm.getConfirmPassword())) {
	        bindingResult.rejectValue("confirmPassword", "signup.password.mismatch", "비밀번호가 일치하지 않습니다.");
	    }

	    // 유효성 검사 실패 시 다시 폼으로
	    if (bindingResult.hasErrors()) {
	        log.warn("회원가입 유효성 검사 실패: {}", bindingResult.getAllErrors());
	        return "user/signup";
	    }

	    try {
	        User user = userAppService.register(
	                signupForm.getEmail(),
	                signupForm.getName(),
	                signupForm.getPassword()
	        );
	        log.info("회원가입 성공 - 생성된 사용자 ID: {}", user.getId());

	        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.");
	        return "redirect:/login?signup=success";

	    } catch (DomainException e) {
	        log.error("DomainException 발생: {}", e.getMessage(), e);
	        if (e.getMessage().contains("이메일")) {
	            bindingResult.rejectValue("email", "signup.email.duplicate", e.getMessage());
	        } else {
	            model.addAttribute("errorMessage", e.getMessage());
	        }
	        return "user/signup";

	    } catch (IllegalArgumentException e) {
	        log.error("IllegalArgumentException 발생: {}", e.getMessage(), e);
	        model.addAttribute("errorMessage", e.getMessage());
	        return "user/signup";

	    } catch (Exception e) {
	        log.error("예상치 못한 Exception 발생", e);
	        model.addAttribute("errorMessage", "회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
	        return "user/signup";
	    }
	}

	/**
	 * 로그아웃 처리 (GET 방식)
	 */
	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
			log.info("사용자 로그아웃: {}", auth.getName());
		}
		return "redirect:/login?logout";
	}	
}
