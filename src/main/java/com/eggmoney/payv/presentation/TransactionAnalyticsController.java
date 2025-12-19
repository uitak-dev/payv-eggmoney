package com.eggmoney.payv.presentation;

import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.eggmoney.payv.application.service.CategoryAppService;
import com.eggmoney.payv.application.service.TransactionAppService;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.entity.Transaction;
import com.eggmoney.payv.domain.model.entity.TransactionType;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ledgers/{ledgerId}/insights")
public class TransactionAnalyticsController {

	private final TransactionAppService transactionAppService;
    private final CategoryAppService categoryAppService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson 사용
    
    
    @GetMapping("/reports")
    public String reports(@PathVariable String ledgerId,
                          @RequestParam(value = "month", required = false) String monthParam,
                          @RequestParam(value = "year", required = false) String yearParam,
                          @RequestParam(value = "tab", required = false, defaultValue = "categories") String tab,
                          Model model) throws Exception {
        LedgerId lId = LedgerId.of(ledgerId);

        // ===== 카테고리별 지출 (month) =====
        YearMonth ym = (monthParam == null || monthParam.trim().isEmpty())
                ? YearMonth.now()
                : YearMonth.parse(monthParam.trim()); // YYYY-MM

        List<Transaction> txns = transactionAppService.listByMonth(lId, ym, Integer.MAX_VALUE, 0);
        List<Category> categories = categoryAppService.listByLedger(lId);

        Map<String, Category> catById = categories.stream()
                .collect(Collectors.toMap(c -> c.getId().toString(), c -> c));
        Map<String, String> rootNameById = categories.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toMap(c -> c.getId().toString(), Category::getName));

        Map<String, String> toRootId = new HashMap<>();
        for (Category c : categories) {
            String id = c.getId().toString();
            if (c.getParentId() == null) {
                toRootId.put(id, id);
            } else {
                String pid = c.getParentId().toString();
                Category p = catById.get(pid);
                while (p != null && p.getParentId() != null) {
                    pid = p.getParentId().toString();
                    p = catById.get(pid);
                }
                toRootId.put(id, (p != null ? p.getId().toString() : id));
            }
        }

        Map<String, Long> expenseByRoot = new HashMap<>();
        for (Transaction t : txns) {
            if (t.getType() == TransactionType.INCOME) continue;
            String catId = t.getCategoryId().toString();
            String rootId = toRootId.getOrDefault(catId, catId);
            expenseByRoot.merge(rootId, t.getAmount().toLong(), Long::sum);
        }

        List<List<Object>> catRows = new ArrayList<>();
        catRows.add(Arrays.asList("카테고리", "지출"));
        expenseByRoot.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> {
                    String rootId = e.getKey();
                    String name = rootNameById.getOrDefault(rootId, rootId);
                    catRows.add(Arrays.asList(name, e.getValue()));
                });

        String pieDataJson = objectMapper.writeValueAsString(catRows);
        long totalOut = expenseByRoot.values().stream().mapToLong(Long::longValue).sum();

        // ===== 월별 수입/지출 (year) =====
        Year year = (yearParam == null || yearParam.trim().isEmpty())
                ? Year.now()
                : Year.parse(yearParam.trim());

        long[] income = new long[12];
        long[] expense = new long[12];
        for (int m = 1; m <= 12; m++) {
            YearMonth y = YearMonth.of(year.getValue(), m);
            List<Transaction> monthTxns = transactionAppService.listByMonth(lId, y, Integer.MAX_VALUE, 0);
            for (Transaction t : monthTxns) {
                long won = t.getAmount().toLong();
                if (t.getType() == TransactionType.INCOME) income[m - 1] += won;
                else expense[m - 1] += won;
            }
        }

        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("월", "수입", "지출"));
        for (int m = 1; m <= 12; m++) {
            rows.add(Arrays.asList(m + "월", income[m - 1], expense[m - 1]));
        }
        String chartDataJson = objectMapper.writeValueAsString(rows);

        long sumIn = Arrays.stream(income).sum();
        long sumOut = Arrays.stream(expense).sum();

        // ===== 모델 =====
        model.addAttribute("ledgerId", ledgerId);
        model.addAttribute("currentPage", "reports");

        // 카테고리
        model.addAttribute("month", ym.toString());
        model.addAttribute("pieDataJson", pieDataJson);
        model.addAttribute("totalExpense", totalOut);

        // 연도별
        model.addAttribute("year", String.valueOf(year.getValue()));
        model.addAttribute("chartDataJson", chartDataJson);
        model.addAttribute("sumIncome", sumIn);
        model.addAttribute("sumExpense", sumOut);
        model.addAttribute("prevYear", String.valueOf(year.minusYears(1).getValue()));
        model.addAttribute("nextYear", String.valueOf(year.plusYears(1).getValue()));
        model.addAttribute("tab", tab); // 현재 탭 정보를 JSP에 내려줌

        return "insights/reports";
    }




}
