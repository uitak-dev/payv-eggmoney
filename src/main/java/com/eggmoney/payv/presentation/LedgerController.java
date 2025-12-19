package com.eggmoney.payv.presentation;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.eggmoney.payv.application.service.LedgerAppService;
import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.security.CustomUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 가계부 컨트롤러
 * @author 정의탁
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/ledgers")
public class LedgerController {

	private final LedgerAppService ledgerAppService;

	@GetMapping
	public String list(Authentication authentication, Model model) {
		
		CustomUser customUser = (CustomUser) authentication.getPrincipal();
		
		// (jw)
		// 사용자 ID를 사용하여 해당 사용자의 가계부 목록을 가져옴
		List<Ledger> ledgers = ledgerAppService.listByOwner(customUser.getUserId());

		// 첫 번째 가계부를 현재 선택된 가계부로 설정 (여기서는 예시로 첫 번째 가계부를 사용)
		if (!ledgers.isEmpty()) {
	        Ledger first = ledgers.get(0);
	        model.addAttribute("currentLedgerId", first.getId().toString());   // ✅ 추가
	        model.addAttribute("currentAccountName", first.getName());
	    }
		// 가계부 목록을 모델에 추가
		model.addAttribute("ledgers", ledgers);
		// (/jw)
		// TODO: 애플리케이션 서비스에 조회 기능이 없다면 Query용 리더(Mapper) 추가 권장
		model.addAttribute("ledgers", ledgerAppService.listByOwner(customUser.getUserId()));
		return "ledgers/list";
	}

	@GetMapping("/new")
	public String newForm() {
		return "ledgers/new";
	}

	@PostMapping
	public String create(Authentication authentication, 
						 @RequestParam("name") String name) {
		CustomUser customUser = (CustomUser) authentication.getPrincipal();
		ledgerAppService.createLedger(customUser.getUserId(), name);
		return "redirect:/ledgers";
	}
	
}
