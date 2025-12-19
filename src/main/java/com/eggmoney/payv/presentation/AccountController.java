package com.eggmoney.payv.presentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eggmoney.payv.application.service.AccountAppService;
import com.eggmoney.payv.application.service.LedgerAppService;
import com.eggmoney.payv.domain.model.entity.Account;
import com.eggmoney.payv.domain.model.entity.AccountType;
import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.presentation.dto.AccountCreateDto;
import com.eggmoney.payv.presentation.dto.AccountListItemDto;
import com.eggmoney.payv.presentation.dto.AccountUpdateDto;

import lombok.RequiredArgsConstructor;

/**
 * 자산 컨트롤러
 * @author 정의탁
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/ledgers/{ledgerId}/accounts")
public class AccountController {

	private final AccountAppService accountAppService;
//	private final LedgerAppService ledgerAppService;
	
    // 공통: enum 목록을 모델에 노출 (폼에서 사용)
    @ModelAttribute("accountTypes")
    public AccountType[] accountTypes() {
        return AccountType.values();
    }

    // ===== 목록 =====
    @GetMapping
    public String list(@PathVariable String ledgerId,
                       @ModelAttribute("message") String message,
                       @ModelAttribute("error") String error,
                       Model model) {

        LedgerId lId = LedgerId.of(ledgerId);
        List<Account> accounts = accountAppService.listByLedger(lId);

        List<AccountListItemDto> items = accounts.stream()
        		.map(this::toListItem)
        		.collect(Collectors.toList());

        model.addAttribute("ledgerId", ledgerId);
        model.addAttribute("accounts", items);
        
        // (jw)
        
        model.addAttribute("currentPage", "accounts"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
        // (jw)
        return "accounts/list";
    }

    // ===== 신규 폼 =====
    @GetMapping("/new")
    public String newForm(@PathVariable String ledgerId, Model model) {
        model.addAttribute("ledgerId", ledgerId);
        model.addAttribute("form", new AccountCreateDto());
        model.addAttribute("currentPage", "accounts"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
        return "accounts/new";
    }

    // ===== 신규 생성 처리 =====
    @PostMapping
    public String create(@PathVariable String ledgerId,
                         @ModelAttribute("form") AccountCreateDto form,
                         RedirectAttributes ra) {
        try {
            if (isBlank(form.getName()) || isBlank(form.getType())) {
                ra.addFlashAttribute("error", "이름과 유형은 필수입니다.");
                return "redirect:/ledgers/" + ledgerId + "/accounts/new";
            }
            LedgerId lId = LedgerId.of(ledgerId);
            AccountType type = AccountType.valueOf(form.getType());

            long won = 0L;
            if (!isBlank(form.getOpeningBalanceWon())) {
                try {
                    won = Long.parseLong(form.getOpeningBalanceWon().trim());
                    if (won < 0) throw new NumberFormatException("음수 금액");
                } catch (NumberFormatException e) {
                    ra.addFlashAttribute("error", "초기 잔액은 0 이상의 정수만 입력 가능합니다.");
                    return "redirect:/ledgers/" + ledgerId + "/accounts/new";
                }
            }

            accountAppService.createAccount(lId, type, form.getName().trim(), Money.won(won));

            ra.addFlashAttribute("message", "계좌를 생성했습니다.");
            return "redirect:/ledgers/" + ledgerId + "/accounts";
        } 
        catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ledgers/" + ledgerId + "/accounts/new";
        }
    }

    // ===== 수정 폼(현재는 자신명만 변경 가능) =====
    @GetMapping("/{accountId}/edit")
    public String editForm(@PathVariable String ledgerId, @PathVariable String accountId,
                           RedirectAttributes ra, Model model) {
    	
        Account acc = accountAppService.getDetails(AccountId.of(accountId));
        
        model.addAttribute("ledgerId", ledgerId);
        model.addAttribute("account", acc);
        model.addAttribute("form", new AccountUpdateDto(acc.getName()));
        model.addAttribute("currentPage", "accounts"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
        return "accounts/edit";
    }

    // ===== 수정 처리(현재는 자신명만 변경 가능) =====
    @PostMapping("/{accountId}")
    public String update(@PathVariable String ledgerId, @PathVariable String accountId,
                         @ModelAttribute("form") AccountUpdateDto form, RedirectAttributes ra) {
        try {
            if (isBlank(form.getName())) {
                ra.addFlashAttribute("error", "자산명은 필수입니다.");
                return "redirect:/ledgers/" + ledgerId + "/accounts/" + accountId + "/edit";
            }
            
            // AccountType type = AccountType.valueOf(form.getType());
            
            accountAppService.rename(AccountId.of(accountId), LedgerId.of(ledgerId), form.getName().trim());

            ra.addFlashAttribute("message", "자산 정보를 수정했습니다.");
            return "redirect:/ledgers/" + ledgerId + "/accounts";
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ledgers/" + ledgerId + "/accounts/" + accountId + "/edit";
        }
    }

    // ===== 삭제 (AJAX, JSON) =====
    @DeleteMapping(value="/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> deleteAjax(@PathVariable String ledgerId,
                                          @PathVariable String accountId) {
        Map<String, Object> res = new HashMap<>();
        try {
            // 해당 자산에 대한 거래 내역이 남아있더라도, 소프트 삭제 진행.
            accountAppService.delete(LedgerId.of(ledgerId), AccountId.of(accountId));
            res.put("ok", true);
        } catch (DomainException e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    // ===== helpers =====
    private AccountListItemDto toListItem(Account a) {
        AccountListItemDto dto = new AccountListItemDto();
        dto.setId(a.getId().toString());
        dto.setName(a.getName());
        dto.setType(a.getType().name());
        // Money를 단순 문자열로 출력.(형식화가 필요하면 별도 헬퍼 사용)
        dto.setBalance(String.valueOf(a.getCurrentBalance())); 
        return dto;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
