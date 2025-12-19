package com.eggmoney.payv.presentation;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eggmoney.payv.application.service.BudgetAppService;
import com.eggmoney.payv.application.service.CategoryAppService;
import com.eggmoney.payv.domain.model.entity.Budget;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.vo.BudgetId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.presentation.dto.BudgetCreateDto;
import com.eggmoney.payv.presentation.dto.BudgetListItemDto;
import com.eggmoney.payv.presentation.dto.BudgetUpdateDto;

import lombok.RequiredArgsConstructor;

/**
 * 예산 컨트롤러
 * 
 * @author 정의탁
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/ledgers/{ledgerId}/budgets")
public class BudgetController {

	private final BudgetAppService budgetAppService;
	private final CategoryAppService categoryAppService;

	// ===== 목록 =====
	@GetMapping
	public String list(@PathVariable String ledgerId, 
					   @RequestParam(value = "month", required = false) String month,
					   @ModelAttribute("message") String message, 
					   @ModelAttribute("error") String error, Model model) {

		YearMonth ym = (month == null || month.trim().isEmpty()) ? 
				YearMonth.now() : YearMonth.parse(month.trim()); // yyyy-MM

		LedgerId lId = LedgerId.of(ledgerId);

		// 월별 예산 목록.
		List<Budget> budgets = budgetAppService.listByLedgerAndMonth(lId, ym);

		// 카테고리 이름 매핑.
		Map<String, String> categoryNameMap = categoryAppService.listByLedger(lId).stream().collect(
				Collectors.toMap(c -> c.getId().toString(), Category::getName, (a, b) -> a, LinkedHashMap::new));

		// DTO 매핑
		List<BudgetListItemDto> items = budgets.stream().map(b -> {
			BudgetListItemDto d = new BudgetListItemDto();
			d.setId(b.getId().toString());
			d.setCategoryId(b.getCategoryId().toString());
			d.setCategoryName(categoryNameMap.getOrDefault(b.getCategoryId().toString(), b.getCategoryId().toString()));
			d.setMonth(ym.toString());
			d.setLimit(String.valueOf(b.getLimit())); // Money.toString()
			d.setSpent(String.valueOf(b.getSpent()));
			return d;
		}).collect(Collectors.toList());

		// 정렬: 카테고리명(한글/영문 혼재 시 필요에 따라 커스터마이즈)
		items.sort(Comparator.comparing(BudgetListItemDto::getCategoryName, String.CASE_INSENSITIVE_ORDER));

		model.addAttribute("ledgerId", ledgerId);
		model.addAttribute("month", ym.toString());
		model.addAttribute("budgets", items);
		model.addAttribute("currentPage", "budgets"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)

		return "budgets/list";
	}

	// ===== 신규 폼 =====
	@GetMapping("/new")
	public String newForm(@PathVariable String ledgerId, 
						  @RequestParam(value = "month", required = false) String month, Model model) {

		YearMonth ym = (month == null || month.trim().isEmpty()) ? YearMonth.now() : YearMonth.parse(month.trim());

		LedgerId lId = LedgerId.of(ledgerId);
		
		List<Category> rootCategories = categoryAppService.rootCategoryListByLedger(lId);
		
		model.addAttribute("ledgerId", ledgerId);
		model.addAttribute("month", ym.toString());
		model.addAttribute("rootCategories", rootCategories);

		BudgetCreateDto form = new BudgetCreateDto();
		form.setMonth(ym.toString());
		form.setLimit("0");
		model.addAttribute("form", form);
		model.addAttribute("currentPage", "budgets"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
		return "budgets/new";
	}

	// ===== 신규 처리 =====
	@PostMapping
	public String create(@PathVariable String ledgerId, @ModelAttribute("form") BudgetCreateDto form,
			RedirectAttributes ra) {
		
		try {
			if (isBlank(form.getMonth()) || isBlank(form.getCategoryId()) || isBlank(form.getLimit())) {
				ra.addFlashAttribute("error", "월/카테고리/한도는 필수입니다.");
				return "redirect:/ledgers/" + ledgerId + "/budgets/new?month=" + safe(form.getMonth());
			}
			YearMonth ym = YearMonth.parse(form.getMonth().trim());
			long won = parseWon(form.getLimit());
			if (won < 0) {
				ra.addFlashAttribute("error", "한도는 0 이상의 정수만 가능합니다.");
				return "redirect:/ledgers/" + ledgerId + "/budgets/new?month=" + ym;
			}

			budgetAppService.createBudget(LedgerId.of(ledgerId), 
					CategoryId.of(form.getCategoryId()), ym, Money.won(won));

			ra.addFlashAttribute("message", "예산을 추가했습니다.");
			return "redirect:/ledgers/" + ledgerId + "/budgets?month=" + ym;

		} catch (DomainException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/ledgers/" + ledgerId + "/budgets/new?month=" + safe(form.getMonth());
		}
	}

	// ===== 수정 폼 (한도 변경) =====
	@GetMapping("/{budgetId}/edit")
	public String editForm(@PathVariable String ledgerId, @PathVariable String budgetId,
			@RequestParam("month") String month, // 컨텍스트 유지
			Model model, RedirectAttributes ra) {
		try {
			Budget b = budgetAppService.getDetails(BudgetId.of(budgetId));
			BudgetUpdateDto form = new BudgetUpdateDto(month, String.valueOf(b.getLimit().toLong()));

			// 카테고리 이름 표기용
			LedgerId lId = LedgerId.of(ledgerId);
			String categoryName = categoryAppService.getDetails(b.getCategoryId()).getName();

			model.addAttribute("ledgerId", ledgerId);
			model.addAttribute("month", month);
			model.addAttribute("budget", b);
			model.addAttribute("categoryName", categoryName);
			model.addAttribute("form", form);
			model.addAttribute("currentPage", "budgets"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
			return "budgets/edit";
		} catch (DomainException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/ledgers/" + ledgerId + "/budgets?month=" + month;
		}
	}

	// ===== 수정 처리 (한도 변경) =====
	@PostMapping("/{budgetId}")
	public String update(@PathVariable String ledgerId, @PathVariable String budgetId,
			@ModelAttribute("form") BudgetUpdateDto form, RedirectAttributes ra) {
		try {
			YearMonth ym = YearMonth.parse(form.getMonth().trim());
			long won = parseWon(form.getLimit());
			if (won < 0) {
				ra.addFlashAttribute("error", "한도는 0 이상의 정수만 가능합니다.");
				return "redirect:/ledgers/" + ledgerId + "/budgets/" + budgetId + "/edit?month=" + ym;
			}
			budgetAppService.changeLimit(BudgetId.of(budgetId), Money.won(won));
			ra.addFlashAttribute("message", "예산 한도를 변경했습니다.");
			return "redirect:/ledgers/" + ledgerId + "/budgets?month=" + ym;
		} 
		catch (DomainException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/ledgers/" + ledgerId + "/budgets/" + budgetId + "/edit?month=" + safe(form.getMonth());
		}
	}

	// ===== helpers =====
	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private String safe(String s) {
		return s == null ? "" : s;
	}

	private long parseWon(String s) {
		try {
			return Long.parseLong(s.trim());
		} catch (Exception e) {
			return -1L;
		}
	}
}
