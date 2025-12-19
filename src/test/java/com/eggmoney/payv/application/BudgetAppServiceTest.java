package com.eggmoney.payv.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.application.service.AccountAppService;
import com.eggmoney.payv.application.service.BudgetAppService;
import com.eggmoney.payv.application.service.CategoryAppService;
import com.eggmoney.payv.application.service.LedgerAppService;
import com.eggmoney.payv.application.service.TransactionAppService;
import com.eggmoney.payv.application.service.UserAppService;
import com.eggmoney.payv.domain.model.entity.Account;
import com.eggmoney.payv.domain.model.entity.AccountType;
import com.eggmoney.payv.domain.model.entity.Budget;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.domain.model.entity.Transaction;
import com.eggmoney.payv.domain.model.entity.TransactionType;
import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.shared.error.DomainException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/root-context.xml")
@Transactional
public class BudgetAppServiceTest {

	@Resource UserAppService userAppService;
    @Resource LedgerAppService ledgerAppService;
    @Resource AccountAppService accountAppService;
    @Resource CategoryAppService categoryAppService;
    @Resource TransactionAppService transactionAppService;
    @Resource BudgetAppService budgetAppService;

    private LedgerId ledgerId;
    private Account  account;
    private Category rootFood;     // 루트 카테고리: 식비
    private Category childLunch;   // 자식 카테고리: 점심

    private static String email(){ return "u_" + UUID.randomUUID().toString().substring(0,8) + "@test.local"; }
    private static String ledgerName(){ return "ledger_" + UUID.randomUUID().toString().substring(0,8); }
    private static String accountName(){ return "은행_" + UUID.randomUUID().toString().substring(0,6); }

    @Before
    public void setUp() {
        User owner = userAppService.register(email(), "{noop}pw", "소유자");
        Ledger ledger = ledgerAppService.createLedger(owner.getId(), ledgerName());
        ledgerId = ledger.getId();
        account = accountAppService.createAccount(ledgerId, AccountType.CASH, accountName(), Money.won(1_000_000));

        rootFood   = categoryAppService.createRoot(ledgerId, "식비", false, 0);
        childLunch = categoryAppService.createChild(ledgerId, rootFood.getId(), "점심", false, 0);
    }
    
    // 유틸: 소비/지출 트랜잭션 생성 + 게시.
    private Transaction postExpense(LocalDate date, long won, CategoryId catId, String memo) {
        return transactionAppService.oneClickCreate(
                ledgerId, account.getId(), TransactionType.EXPENSE,
                date, Money.won(won), catId, memo
        );
    }
    
    // ---- 1) 현재 달: 자식 카테고리 예산 생성 시, 이미 게시된 지출 합산하여 spent 세팅.
    @Test
    public void create_currentMonth_childBudget_initialSpentFromPosted() {
        LocalDate base = LocalDate.now();
        // 범위 내 2건(합계 = 12_000), 범위 밖 1건(9_999)
        postExpense(base.withDayOfMonth(3),  5_000, childLunch.getId(), "in-a");
        postExpense(base.withDayOfMonth(20), 7_000, childLunch.getId(), "in-b");
        postExpense(base.minusMonths(1).withDayOfMonth(28), 9_999, childLunch.getId(), "out-prev");

        Budget budget = budgetAppService.createBudget(ledgerId, childLunch.getId(), YearMonth.now(), Money.won(10_000));
        
        assertEquals(12_000L, budget.getSpent().toLong());	// 초기 소진 반영.
        assertTrue(budget.isExceeded());                    // limit(10k) < spent(12k) → 초과 허용.
    }

    // ---- 2) 현재 달: 루트 카테고리 예산은 루트 + 하위 카테고리 지출 합산.
    @Test
    public void create_currentMonth_rootBudget_includesChildren() {
        LocalDate base = LocalDate.now();
        // 범위 내 2건(합계 = 5_000), 범위 밖 1건(1_234)
        postExpense(base.withDayOfMonth(2),  2_000, rootFood.getId(),   "root-in");
        postExpense(base.withDayOfMonth(10), 3_000, childLunch.getId(), "child-in");
        postExpense(base.plusMonths(1).withDayOfMonth(1), 1_234, childLunch.getId(), "out-next");

        Budget budget = budgetAppService.createBudget(ledgerId, rootFood.getId(), YearMonth.now(), Money.won(10_000));
        
        assertEquals(5_000L, budget.getSpent().toLong());	// 2_000 + 3_000
        assertFalse(budget.isExceeded());
    }

    // ---- 3) 미래 달: 생성 허용(spent=0).
    @Test
    public void create_futureMonth_allowed_spentZero() {
        YearMonth next = YearMonth.now().plusMonths(1);
        
        // 미래 달의 거래는 아직 없다고, 가정(있어도 서비스는 기간 필터로 현재 달만 합산)
        Budget budget = budgetAppService.createBudget(ledgerId, childLunch.getId(), next, Money.won(50_000));

        assertEquals(0L, budget.getSpent().toLong());
        assertFalse(budget.isExceeded());
    }

    // ---- 4) 과거 달: 생성 금지.
    @Test(expected = DomainException.class)
    public void create_pastMonth_forbidden() {
        YearMonth prev = YearMonth.now().minusMonths(1);
        budgetAppService.createBudget(ledgerId, childLunch.getId(), prev, Money.won(10_000));
    }

    // ---- 5) 상호배타: 자식 예산이 있으면 같은 월 루트 예산 금지.
    @Test(expected = DomainException.class)
    public void mutualExclusion_childThenRoot_forbidden() {
        YearMonth month = YearMonth.now();
        budgetAppService.createBudget(ledgerId, childLunch.getId(), month, Money.won(10_000));
        budgetAppService.createBudget(ledgerId, rootFood.getId(), month, Money.won(20_000));	// 금지
    }

    // ---- 6) 상호배타: 루트 예산이 있으면 같은 월 자식 예산 금지.
    @Test(expected = DomainException.class)
    public void mutualExclusion_rootThenChild_forbidden() {
        YearMonth month = YearMonth.now();
        budgetAppService.createBudget(ledgerId, rootFood.getId(), month, Money.won(30_000));
        budgetAppService.createBudget(ledgerId, childLunch.getId(), month, Money.won(10_000));	// 금지
    }

    // ---- 7) 동일 (ledger, category, month) 중복 생성 금지.
    @Test(expected = DomainException.class)
    public void duplicate_sameCategorySameMonth_forbidden() {
        YearMonth month = YearMonth.now();
        budgetAppService.createBudget(ledgerId, childLunch.getId(), month, Money.won(10_000));
        budgetAppService.createBudget(ledgerId, childLunch.getId(), month, Money.won(20_000));	// 금지
    }

    // ---- 8) 한도 변경: 현재 달 허용, 값 반영.
    @Test
    public void changeLimit_currentMonth_success() {
        YearMonth month = YearMonth.now();
        Budget budget = budgetAppService.createBudget(ledgerId, childLunch.getId(), month, Money.won(10_000));
        
        budgetAppService.changeLimit(budget.getId(), Money.won(25_000));
        
        // 재조회 대신 객체 자체의 변경을 확인(Repository가 같은 인스턴스를 반환하지 않는다면 재조회로 검증)
        assertEquals(25_000L, budgetAppService.getDetails(budget.getId()).getLimit().toLong());
    }
    
    
    /** 특정 월의 모든 예산 목록: 해당 월 것만 반환되어야 함 */
    @Test
    public void listByLedgerAndMonth_returnsBudgetsForThatMonthOnly() {
        YearMonth ymNow  = YearMonth.now();
        YearMonth ymNext = ymNow.plusMonths(1);

        // 추가 카테고리 트리(서로 다른 루트로 상호배타 규칙 회피)
        Category rootTrans = categoryAppService.createRoot(ledgerId, "교통", false, 1);
        // 이번 달 예산 2건 (서로 다른 루트 트리에서 1건씩)
        Budget b1 = budgetAppService.createBudget(ledgerId, childLunch.getId(), ymNow,  Money.won(10_000)); // 점심(자식)
        Budget b2 = budgetAppService.createBudget(ledgerId, rootTrans.getId(), ymNow,  Money.won(20_000)); // 교통(루트)

        // 다른 달(다음 달) 예산 — 목록에서 제외되어야 함
        budgetAppService.createBudget(ledgerId, childLunch.getId(), ymNext, Money.won(11_000));
        budgetAppService.createBudget(ledgerId, rootTrans.getId(), ymNext, Money.won(21_000));

        // when )
        List<Budget> list = budgetAppService.listByLedgerAndMonth(ledgerId, ymNow);

        // then )
        // 모두 해당 월이어야 함.
        assertTrue(list.stream().allMatch(b -> b.getMonth().equals(ymNow)));
        // 이번 달 생성한 두 건이 포함되어야 함.
        Set<String> ids = list.stream().map(b -> b.getId().value()).collect(Collectors.toSet());
        assertTrue(ids.contains(b1.getId().value()));
        assertTrue(ids.contains(b2.getId().value()));
        // 다음 달 건은 포함되면 안 됨.
        assertEquals(2, list.size());
    }

    /** 월별 시리즈: 존재하는 달만, 시간순으로 반환 */
    @Test
    public void listMonthlySeries_returnsExistingBudgetsOnlyInRange() {
        YearMonth m0 = YearMonth.now();
        YearMonth m1 = m0.plusMonths(1);
        YearMonth m2 = m0.plusMonths(2);
        YearMonth m3 = m0.plusMonths(3);

        // childLunch 카테고리에 대해 m0, m1, m3만 생성 (m2는 없음)
        Budget bm0 = budgetAppService.createBudget(ledgerId, childLunch.getId(), m0, Money.won(10_000));
        Budget bm1 = budgetAppService.createBudget(ledgerId, childLunch.getId(), m1, Money.won(12_000));
        Budget bm3 = budgetAppService.createBudget(ledgerId, childLunch.getId(), m3, Money.won(14_000));

        // when
        List<Budget> series = budgetAppService.listMonthlySeries(ledgerId, childLunch.getId(), m0, m3);

        // then
        // 존재하는 달만 반환(m0, m1, m3) — m2는 없어야 함.
        List<YearMonth> months = series.stream().map(Budget::getMonth).collect(Collectors.toList());
        assertEquals(Arrays.asList(m0, m1, m3), months);
        // ID 매칭(선택적 검증)
        Set<String> returnedIds = series.stream().map(b -> b.getId().value()).collect(Collectors.toSet());
        assertTrue(returnedIds.contains(bm0.getId().value()));
        assertTrue(returnedIds.contains(bm1.getId().value()));
        assertTrue(returnedIds.contains(bm3.getId().value()));
    }
}
