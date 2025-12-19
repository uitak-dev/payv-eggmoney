package com.eggmoney.payv.presentation;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.eggmoney.payv.application.service.LedgerAppService;
import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.domain.model.vo.LedgerId;

import lombok.RequiredArgsConstructor;

/**
 * @RequestMapping("/ledgers/{ledgerId}/...") 구조로 매핑된 모든 컨트롤러에서
 * aside.jsp가 정상 동작하도록 현재 선택된 가계부 정보(ledgerId, ledgerName)를
 * 공통으로 Model에 주입한다.
 * 
 * @author 한지원 
 */
@ControllerAdvice(assignableTypes = { 
        AccountController.class, 
        BudgetController.class, 
        CategoryController.class,
        TransactionController.class, 
        TransactionAnalyticsController.class,
//        CalendarController.class
})
@RequiredArgsConstructor
public class LedgerModelAdvice {

    private final LedgerAppService ledgerAppService;

    @ModelAttribute
    public void addLedgerInfo(@PathVariable(value = "ledgerId", required = false) String ledgerId,
                              Model model) {
        if (ledgerId != null) {
            model.addAttribute("currentLedgerId", ledgerId);

            // 가계부 이름 표시
            LedgerId lId = LedgerId.of(ledgerId);
            Ledger ledger = ledgerAppService.getLedger(lId);
            model.addAttribute("currentAccountName", ledger.getName());
        }
    }
    
}
