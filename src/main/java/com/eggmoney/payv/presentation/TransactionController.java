package com.eggmoney.payv.presentation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eggmoney.payv.application.service.AccountAppService;
import com.eggmoney.payv.application.service.CategoryAppService;
import com.eggmoney.payv.application.service.TransactionAppService;
import com.eggmoney.payv.domain.model.entity.Account;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.entity.Transaction;
import com.eggmoney.payv.domain.model.entity.TransactionType;
import com.eggmoney.payv.domain.model.vo.AccountId;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.model.vo.TransactionId;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.presentation.dto.CategoryOptionDto;
import com.eggmoney.payv.presentation.dto.PageRequestDto;
import com.eggmoney.payv.presentation.dto.PageResultDto;
import com.eggmoney.payv.presentation.dto.TransactionCalendarDayDto;
import com.eggmoney.payv.presentation.dto.TransactionCalendarWeekDto;
import com.eggmoney.payv.presentation.dto.TransactionCreateDto;
import com.eggmoney.payv.presentation.dto.TransactionListItemDto;
import com.eggmoney.payv.presentation.dto.TransactionSearchCondition;
import com.eggmoney.payv.presentation.dto.TransactionUpdateDto;

import lombok.RequiredArgsConstructor;

/**
 * 거래 내역 컨트롤러
 * @author 정의탁
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/ledgers/{ledgerId}/transaction")
public class TransactionController {

	private final TransactionAppService transactionAppService;
	private final AccountAppService accountAppService;
	private final CategoryAppService categoryAppService;

	// 폼에서 사용할 enum 목록.
	@ModelAttribute("transactionTypes")
	public TransactionType[] tansactionTypes() {
		return TransactionType.values();
	}

	// 거래 내역 목록 조회.
	@GetMapping
	public String list(@PathVariable String ledgerId,
	                   @ModelAttribute TransactionSearchCondition cond,
	                   @ModelAttribute PageRequestDto page, 
	                   Model model, 
	                   @ModelAttribute("message") String message,
	                   @ModelAttribute("error") String error) {

	    LedgerId lId = LedgerId.of(ledgerId);
	    
	    // 1) 기본값 보정 (이번 달)
	    YearMonth ym = YearMonth.now();
	    if (cond.getStart() == null) cond.setStart(ym.atDay(1));
	    if (cond.getEnd()   == null) cond.setEnd(ym.atEndOfMonth());
	    
	    if (cond.getEnd().isBefore(cond.getStart())) {
	        LocalDate tmp = cond.getStart();
	        cond.setStart(cond.getEnd());
	        cond.setEnd(tmp);
	    }
	    
	    if (page.getSize() <= 0) page.setSize(20);
	    if (page.getPage() <= 0) page.setPage(1);

	    // 2) 필터 바 표시용 데이터
	    List<Account> accounts = accountAppService.listByLedger(lId);
	    List<Category> roots   = categoryAppService.rootCategoryListByLedger(lId);

	    // 3) 카테고리 해석(하위 포함 집합 구하기)
	    if (cond.getCategoryId() != null && !cond.getCategoryId().trim().isEmpty()) {
	        cond.setResolvedCategoryIds(Collections.singletonList(cond.getCategoryId().trim()));
	    } else if (cond.getRootCategoryId() != null && !cond.getRootCategoryId().trim().isEmpty()) {
	        CategoryId parentId = CategoryId.of(cond.getRootCategoryId().trim());
	        List<Category> children = categoryAppService
	                .subCategoryListByLedgerAndParentCategory(lId, parentId);
	        List<String> ids = new ArrayList<>();
	        ids.add(parentId.toString()); // 상위 포함
	        for (Category c : children) {
	        	ids.add(c.getId().toString());
	        }
	        cond.setResolvedCategoryIds(ids);
	    } else {
	        cond.setResolvedCategoryIds(null); // 전체
	    }

	    // 4) 서비스 호출 (페이지 결과) : search() -> findListByCondition
	    PageResultDto<Transaction> pr = transactionAppService.search(lId, cond, page);
	    
	    // 5) 표시용 이름 맵( {id : name} )
	    Map<String, String> accountNameMap = accounts.stream()
	            .collect(Collectors.toMap(a -> a.getId().toString(), Account::getName, (a,b)->a, LinkedHashMap::new));
	    Map<String, String> categoryNameMap = categoryAppService.listByLedger(lId).stream()
	            .collect(Collectors.toMap(c -> c.getId().toString(), Category::getName, (a,b)->a, LinkedHashMap::new));

	    List<TransactionListItemDto> items = pr.getContent().stream().map(t -> {
	        TransactionListItemDto d = new TransactionListItemDto();
	        d.setId(t.getId().toString());
	        d.setDate(t.getDate().toString());
	        d.setAccountName(accountNameMap.getOrDefault(t.getAccountId().toString(), t.getAccountId().toString()));
	        d.setCategoryName(categoryNameMap.getOrDefault(t.getCategoryId().toString(), t.getCategoryId().toString()));
	        d.setType(t.getType().name());
	        d.setAmount(String.valueOf(t.getAmount()));
	        d.setMemo(t.getMemo());
	        return d;
	    }).collect(Collectors.toList());

	    // 6) 모델
	    model.addAttribute("ledgerId", ledgerId);
	    model.addAttribute("accounts", accounts);
	    model.addAttribute("rootCategories", roots);
		
		 // (jw)월 합계 계산
	    long monthIncome = pr.getContent().stream()
	            .filter(t -> t.getType().name().equals("INCOME"))
	            .mapToLong(t -> t.getAmount().toLong())
	            .sum();
	    long monthExpense = pr.getContent().stream()
	            .filter(t -> !t.getType().name().equals("INCOME"))
	            .mapToLong(t -> t.getAmount().toLong())
	            .sum();

	    // 필터 값 바인딩(폼 name은 DTO 필드명과 동일하게)
	    model.addAttribute("cond", cond);

	    // 목록/페이징
	    model.addAttribute("txns", items);
	    model.addAttribute("page", page.getPage());
	    model.addAttribute("size", page.getSize());
	    model.addAttribute("total", pr.getTotal());
	    model.addAttribute("totalPages", pr.totalPages());
	    model.addAttribute("startPage", Math.max(1, page.getPage() - 2));
	    model.addAttribute("endPage", Math.min(pr.totalPages(), Math.max(1, page.getPage() - 2) + 4));
	    model.addAttribute("hasPrev", page.getPage() > 1);
	    model.addAttribute("hasNext", page.getPage() < pr.totalPages());
	    
	    // 화면에 필요한 정보 추가
	    model.addAttribute("month", ym.toString()); // yyyy-MM
		model.addAttribute("transaction", items);
		model.addAttribute("currentPage", "transaction"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
		model.addAttribute("monthIncome", monthIncome);   // (jw)
	    model.addAttribute("monthExpense", monthExpense); // (jw)
		
		return "transactions/list";
	}

	
	// 거래 내역 달력 조회.
	@GetMapping("/calendar")
	public String calendar(@PathVariable String ledgerId,
						   @RequestParam(value = "month", required = false) String month,
						   Model model) {

		// 1) 파라미터 정규화.
		YearMonth ym = (month == null || month.trim().isEmpty()) ? YearMonth.now()
				: YearMonth.parse(month.trim()); // "YYYY-MM"
		LedgerId lId = LedgerId.of(ledgerId);

		// 2) 해당 월 거래 목록 조회.
		List<Transaction> all = transactionAppService.listByMonth(lId, ym, Integer.MAX_VALUE, 0);

		// 3) 카테고리 표시용 맵.
		Map<String, String> categoryNameMap = categoryAppService.listByLedger(lId).stream()
				.collect(Collectors.toMap(c -> c.getId().toString(),
						Category::getName, (a, b) -> a, LinkedHashMap::new));

		// 4) 날짜별 그룹.
		Map<LocalDate, List<Transaction>> byDate = all.stream()
				.collect(Collectors.groupingBy(Transaction::getDate, TreeMap::new, Collectors.toList()));

		// 5) 달력 범위: 해당 월의 '일요일 시작 ~ 토요일 끝'
		LocalDate first = ym.atDay(1);
		LocalDate last = ym.atEndOfMonth();
		LocalDate gridStart = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		LocalDate gridEnd = last.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

		// 6) 그리드(주 x 7) 구성.
		List<TransactionCalendarWeekDto> weeks = new ArrayList<>();
		LocalDate d = gridStart;
		while (!d.isAfter(gridEnd)) {
			List<TransactionCalendarDayDto> days = new ArrayList<>(7);
			for (int i = 0; i < 7; i++) {
				TransactionCalendarDayDto day = new TransactionCalendarDayDto();
				day.setDate(d.toString());
				day.setInMonth(!d.isBefore(first) && !d.isAfter(last));

				List<com.eggmoney.payv.domain.model.entity.Transaction> list = byDate.getOrDefault(d, Collections.emptyList());

				long incomeWon = 0L, expenseWon = 0L;
				int showLimit = 5; // 셀당 최대 5개만 표시(넘치면 총액으로 확인 가능)
				int cnt = 0;
				for (Transaction t : list) {
					long won = t.getAmount().toLong(); // Money -> long (네 도메인에 맞춰 사용)
					if (t.getType().name().equals("INCOME"))
						incomeWon += won;
					else
						expenseWon += won;

					if (cnt < showLimit) {
						String catName = categoryNameMap.getOrDefault(t.getCategoryId().toString(), "");
						String amtStr = (t.getType().name().equals("INCOME") ? "+" : "-") + String.valueOf(won);
						day.getTxns().add(new TransactionCalendarDayDto.TxnMiniDto(t.getId().toString(), catName,
								amtStr, t.getType().name()));
						cnt++;
					}
				}
				
				day.setIncome(incomeWon > 0 ? "+" + incomeWon : "");
				day.setExpense(expenseWon > 0 ? "-" + expenseWon : "");

				days.add(day);
				d = d.plusDays(1);
			}
			weeks.add(new TransactionCalendarWeekDto(days));
		}

		// 월 합계
		long monthIncome = all.stream().filter(t -> t.getType().name().equals("INCOME"))
				.mapToLong(t -> t.getAmount().toLong()).sum();
		long monthExpense = all.stream().filter(t -> !t.getType().name().equals("INCOME"))
				.mapToLong(t -> t.getAmount().toLong()).sum();

		// 이전/다음 월
		java.time.YearMonth prev = ym.minusMonths(1);
		java.time.YearMonth next = ym.plusMonths(1);

		// 모델
		model.addAttribute("ledgerId", ledgerId);
		model.addAttribute("month", ym.toString()); // yyyy-MM
		model.addAttribute("weeks", weeks);
		model.addAttribute("monthIncome", monthIncome);
		model.addAttribute("monthExpense", monthExpense);
		model.addAttribute("prevMonth", prev.toString());
		model.addAttribute("nextMonth", next.toString());
		model.addAttribute("currentPage", "calendar"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)

		return "transactions/calendar";
	}
	
	
	
	// ===== 신규 폼 =====
	@GetMapping("/new")
	public String newForm(@PathVariable String ledgerId, 
						  @RequestParam(value = "date", required = false) String date, 
						  Model model) {
		
		LedgerId lId = LedgerId.of(ledgerId);
		List<Account> accounts = accountAppService.listByLedger(lId);
		List<Category> rootCategories = categoryAppService.rootCategoryListByLedger(lId);

		// 기본 폼
	    TransactionCreateDto form = defaultCreateForm(); // 기존: 오늘 날짜 등 기본값 세팅.

	    // date=YYYY-MM-DD 가 넘어오면 유효성 검사 후 반영.
	    if (date != null && !date.trim().isEmpty()) {
	        String s = date.trim();
	        try {
	            LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
	            form.setDate(s); // <input type="date"> 에 그대로 들어가도록 ISO 형식 유지
	        } catch (DateTimeParseException ignore) {
	            // 형식이 틀리면 무시하고 기본값 유지.
	        }
	    }
		
		model.addAttribute("ledgerId", ledgerId);
		model.addAttribute("accounts", accounts);
		model.addAttribute("rootCategories", rootCategories);
		model.addAttribute("form", form);
		model.addAttribute("currentPage", "transaction"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
		return "transactions/new";
	}

	// ===== 신규 처리 =====
	@PostMapping
	public String create(@PathVariable String ledgerId, 
						 @ModelAttribute("form") TransactionCreateDto form,
						 RedirectAttributes ra) {
		try {
			// 검증
			if (isBlank(form.getAccountId()) || isBlank(form.getCategoryId()) || isBlank(form.getDate())
					|| isBlank(form.getType()) || isBlank(form.getAmount())) {
				ra.addFlashAttribute("error", "필수 입력값이 누락되었습니다.");
				return "redirect:/ledgers/" + ledgerId + "/transaction/new";
			}

			LedgerId lId = LedgerId.of(ledgerId);
			AccountId accId = AccountId.of(form.getAccountId());
			CategoryId catId = CategoryId.of(form.getCategoryId());
			LocalDate date = LocalDate.parse(form.getDate());
			TransactionType type = TransactionType.valueOf(form.getType());

			long won = parseWon(form.getAmount());
			if (won < 0) {
				ra.addFlashAttribute("error", "금액은 0 이상의 정수만 가능합니다.");
				return "redirect:/ledgers/" + ledgerId + "/transaction/new";
			}
			
			transactionAppService.oneClickCreate(lId, accId, type, date, Money.won(won), catId, form.getMemo());

			ra.addFlashAttribute("message", "거래를 등록했습니다.");
			// 등록 후: 해당 월 목록으로
			String month = YearMonth.from(date).toString();
			return "redirect:/ledgers/" + ledgerId + "/transaction?month=" + month;

		} catch (DomainException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/ledgers/" + ledgerId + "/transaction/new";
		}
	}

	// ===== 수정 폼 =====
	@GetMapping("/{txnId}/edit")
	public String editForm(@PathVariable String ledgerId, @PathVariable String txnId, Model model) {
		// 거래 상세
	    Transaction t = transactionAppService.getDetails(TransactionId.of(txnId));

	    LedgerId lId = LedgerId.of(ledgerId);
	    List<Account> accounts = accountAppService.listByLedger(lId);
	    List<Category> allCategoryList  = categoryAppService.listByLedger(lId);

	    // 1) 루트 카테고리 목록.
	    Comparator<Category> cmp = Comparator
	        .comparingInt(Category::getSortOrder)
	        .thenComparing(Category::getName, String.CASE_INSENSITIVE_ORDER);

	    List<Category> rootCategories = allCategoryList.stream()
	        .filter(c -> c.getParentId() == null)
	        .sorted(cmp)
	        .collect(Collectors.toList());

	    // 2) 현재 거래의 카테고리로부터 (루트/하위) 결정.
	    Category current = allCategoryList.stream()
	        .filter(c -> c.getId().equals(t.getCategoryId()))
	        .findFirst().orElse(null);

	    String selectedRootId = "";
	    String selectedChildId = "";
	    if (current != null) {
	        if (current.getParentId() == null) {
	            selectedRootId = current.getId().toString();
	        } else {
	            selectedRootId = current.getParentId().toString();
	            selectedChildId = current.getId().toString();
	        }
	    }

	    // 3) 폼 DTO (hidden categoryId로 최종 전송)
	    TransactionUpdateDto form = new TransactionUpdateDto(
	        t.getAccountId().toString(),
	        t.getCategoryId().toString(),
	        t.getDate().toString(),
	        t.getType().name(),
	        String.valueOf(t.getAmount().toLong()), // Money → long (네 도메인에 맞춤)
	        t.getMemo()
	    );

	    model.addAttribute("ledgerId", ledgerId);
	    model.addAttribute("transaction", t);
	    model.addAttribute("accounts", accounts);
	    model.addAttribute("rootCategories", rootCategories);
	    model.addAttribute("selectedRootId", selectedRootId);
	    model.addAttribute("selectedChildId", selectedChildId);
	    model.addAttribute("form", form);
	    model.addAttribute("currentPage", "transaction"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)

	    return "transactions/edit";
	}

	// ===== 수정 처리 =====
	@PostMapping("/{txnId}")
	public String update(@PathVariable String ledgerId, @PathVariable String txnId,
			@ModelAttribute("form") TransactionUpdateDto form, RedirectAttributes ra) {
		try {
			if (isBlank(form.getAccountId()) || isBlank(form.getCategoryId()) || isBlank(form.getDate())
					|| isBlank(form.getType()) || isBlank(form.getAmount())) {
				ra.addFlashAttribute("error", "필수 입력값이 누락되었습니다.");
				return "redirect:/ledgers/" + ledgerId + "/transaction/" + txnId + "/edit";
			}

			// LedgerId lId = LedgerId.of(ledgerId);
			AccountId accId = AccountId.of(form.getAccountId());
			CategoryId catId = CategoryId.of(form.getCategoryId());
			LocalDate date = LocalDate.parse(form.getDate());
			TransactionType type = TransactionType.valueOf(form.getType());

			long won = parseWon(form.getAmount());
			if (won < 0) {
				ra.addFlashAttribute("error", "금액은 0 이상의 정수만 가능합니다.");
				return "redirect:/ledgers/" + ledgerId + "/transaction/" + txnId + "/edit";
			}

			transactionAppService.oneClickUpdate(TransactionId.of(txnId), accId, type, date, Money.won(won), catId, form.getMemo());

			ra.addFlashAttribute("message", "거래를 수정했습니다.");
			String month = YearMonth.from(date).toString();
			return "redirect:/ledgers/" + ledgerId + "/transaction?month=" + month;

		} catch (DomainException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/ledgers/" + ledgerId + "/transaction/" + txnId + "/edit";
		}
	}

	// ===== 삭제 (AJAX, JSON) =====
	@DeleteMapping(value = "/{txnId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> deleteAjax(@PathVariable String ledgerId, @PathVariable String txnId) {
		Map<String, Object> res = new HashMap<>();
		try {
			transactionAppService.oneClickDelete(TransactionId.of(txnId));
			res.put("ok", true);
		} catch (DomainException e) {
			res.put("ok", false);
			res.put("message", e.getMessage());
		}
		return res;
	}

	// ===== helpers =====
	private TransactionCreateDto defaultCreateForm() {
		TransactionCreateDto f = new TransactionCreateDto();
		f.setDate(LocalDate.now().toString());
		f.setType(TransactionType.EXPENSE.name()); // 기본값(원하면 변경)
		f.setAmount("0");
		return f;
	}

	private List<CategoryOptionDto> toCategoryOptions(List<Category> list) {
		// 루트/자식 트리를 평탄화해서 select 옵션 라벨 구성
		Map<String, Category> map = new LinkedHashMap<>();
		for (Category c : list)
			map.put(c.getId().toString(), c);
		List<CategoryOptionDto> opts = new ArrayList<>();

		Comparator<Category> cmp = Comparator.comparingInt(Category::getSortOrder).thenComparing(Category::getName,
				String.CASE_INSENSITIVE_ORDER);

		// 루트
		List<Category> roots = list.stream().filter(c -> c.getParentId() == null).sorted(cmp)
				.collect(Collectors.toList());
		for (Category r : roots) {
			opts.add(new CategoryOptionDto(r.getId().toString(), r.getName()));
			// 자식
			List<Category> children = list.stream().filter(c -> r.getId().equals(c.getParentId())).sorted(cmp)
					.collect(Collectors.toList());
			for (Category ch : children) {
				opts.add(new CategoryOptionDto(ch.getId().toString(), "— " + ch.getName()));
			}
		}
		return opts;
	}

	private long parseWon(String s) {
		try {
			return Long.parseLong(s.trim());
		} catch (Exception e) {
			return -1L;
		}
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
