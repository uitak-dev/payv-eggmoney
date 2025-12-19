package com.eggmoney.payv.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.application.service.AccountAppService;
import com.eggmoney.payv.application.service.CategoryAppService;
import com.eggmoney.payv.application.service.LedgerAppService;
import com.eggmoney.payv.application.service.TransactionAppService;
import com.eggmoney.payv.application.service.UserAppService;
import com.eggmoney.payv.domain.model.entity.Account;
import com.eggmoney.payv.domain.model.entity.AccountType;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.domain.model.entity.Transaction;
import com.eggmoney.payv.domain.model.entity.TransactionType;
import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.model.repository.TransactionRepository;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;
import com.eggmoney.payv.domain.model.vo.TransactionId;
import com.eggmoney.payv.domain.shared.error.DomainException;

/**
 * Transaction Application Service Test Class
 * @author 정의탁
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/root-context.xml")
@Transactional
public class TransactionAppServiceTest {

	@Resource UserAppService userAppService;
    @Resource LedgerAppService ledgerAppService;
    @Resource AccountAppService accountAppService;
    @Resource CategoryAppService categoryAppService;
    @Resource TransactionAppService transactionAppService;
    
    @Resource TransactionRepository transactionRepository;

    private LedgerId ledgerId;
    private Account account;
    private Category categoryFood;

    private static String email(){ return "u_" + UUID.randomUUID().toString().substring(0,8) + "@test.local"; }
    private static String ledgerName(){ return "ledger_" + UUID.randomUUID().toString().substring(0,8); }
    private static String accountName(){ return "지갑_" + UUID.randomUUID().toString().substring(0,6); }

    @Before
    public void setUp() {
        User owner = userAppService.register(email(), "{noop}pw", "소유자");
        Ledger ledger = ledgerAppService.createLedger(owner.getId(), ledgerName());
        ledgerId = ledger.getId();
        account = accountAppService.createAccount(ledgerId, AccountType.CASH, accountName(), Money.won(100_000));
        categoryFood = categoryAppService.createRoot(ledgerId, "식비", false, 0);
    }
    
    @Test
    public void createDraft_doesNotAffectBalance() {
        Transaction draft = transactionAppService.create(
                ledgerId, account.getId(), TransactionType.EXPENSE,
                LocalDate.now(), Money.won(12_500), categoryFood.getId(), "점심");
        
        assertNotNull(draft.getId());
        assertFalse(draft.isPosted());
        assertEquals(100_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());
    }

    @Test
    public void post_appliesToBalance() {
        Transaction draft = transactionAppService.create(
                ledgerId, account.getId(), TransactionType.EXPENSE,
                LocalDate.now(), Money.won(10_000), categoryFood.getId(), "식비");
        
        transactionAppService.post(draft.getId());
        
        assertTrue(transactionRepository.findById(draft.getId()).orElseThrow(AssertionError::new).isPosted());
        assertEquals(90_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());
    }

    @Test(expected = DomainException.class)
    public void updatePosted_forbidden() {
        Transaction draft = transactionAppService.create(
                ledgerId, account.getId(), TransactionType.EXPENSE,
                LocalDate.now(), Money.won(5_000), categoryFood.getId(), "커피");
        
        transactionAppService.post(draft.getId());
        // 게시 후 금액 변경은 도메인에서 차단
        transactionAppService.updateDetails(draft.getId(), null, null, null, Money.won(7_000), null, null);
    }

    @Test
    public void unpost_revertsBalance() {
        Transaction draft = transactionAppService.create(
                ledgerId, account.getId(), TransactionType.EXPENSE,
                LocalDate.now(), Money.won(8_000), categoryFood.getId(), "간식");
        
        transactionAppService.post(draft.getId());
        assertEquals(92_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());
        
        transactionAppService.unpost(draft.getId());
        assertEquals(100_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());
    }

    @Test
    public void updateAfterUnpost_thenPost_appliesNewValues() {
        Transaction draft = transactionAppService.create(
                ledgerId, account.getId(), TransactionType.EXPENSE,
                LocalDate.now(), Money.won(12_000), categoryFood.getId(), "점심");
                
        transactionAppService.post(draft.getId());
        transactionAppService.unpost(draft.getId());

        Category dinner = categoryAppService.createRoot(ledgerId, "외식", false, 1);
        transactionAppService.updateDetails(
                draft.getId(), null, null,
                LocalDate.now().minusDays(1), Money.won(13_000), dinner.getId(), "어제 저녁");        
        transactionAppService.post(draft.getId());

        assertEquals(87_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());
    }

    @Test
    public void delete_postedForbidden_thenUnpostAndDelete_success() {
        Transaction draft = transactionAppService.create(
                ledgerId, account.getId(), TransactionType.EXPENSE,
                LocalDate.now(), Money.won(4_000), categoryFood.getId(), "과자");
        transactionAppService.post(draft.getId());

        try {
            transactionAppService.delete(draft.getId());
            fail("게시 상태 삭제는 금지되어야 함");
        } catch (DomainException expected) { /* OK */ }

        transactionAppService.unpost(draft.getId());
        transactionAppService.delete(draft.getId());
        
        assertFalse(transactionRepository.findById(draft.getId()).isPresent());
    }
    
	// 원클릭: 생성 → 게시
	@Test
	public void createAndPost_affectsBalance() {
		Transaction tx = transactionAppService.oneClickCreate(ledgerId, account.getId(), TransactionType.EXPENSE,
				LocalDate.now(), Money.won(10_000), categoryFood.getId(), "저녁");
		assertNotNull(tx.getId());
		assertTrue(transactionRepository.findById(tx.getId()).orElseThrow(AssertionError::new).isPosted());
		assertEquals(90_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());
	}
	
	// 원클릭: (게시 상태) unpost → 수정 → 재게시
	@Test
	public void updateAndRepost_allRequired_overwritesAmountAndMemo() {
		// 먼저 10,000원 지출로 게시
		Transaction tx = transactionAppService.oneClickCreate(ledgerId, account.getId(), TransactionType.EXPENSE,
				LocalDate.now(), Money.won(10_000), categoryFood.getId(), "원본");
		assertEquals(90_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());

		// 카테고리 변경용 새 카테고리
		Category dinner = categoryAppService.createRoot(ledgerId, "외식", false, 1);

		// 10,000 → 13,000으로 변경하고 재게시 (모든 인자 필수)
		Transaction updated = transactionAppService.oneClickUpdate(tx.getId(), account.getId(), // 동일 자산으로 유지
				TransactionType.EXPENSE, LocalDate.now().minusDays(1), Money.won(13_000), dinner.getId(), "수정됨");

		assertTrue(updated.isPosted());
		assertEquals("수정됨", updated.getMemo());
		// 100,000 - 13,000 = 87,000 (기존 10,000는 unpost로 되돌린 다음 13,000 적용)
		assertEquals(87_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());
	}
	
	// 원클릭: unpost → 삭제
    @Test
    public void delete_requiresUnpost_thenDeleteSuccess() {
        Transaction tx = transactionAppService.oneClickCreate(
                ledgerId, account.getId(), TransactionType.EXPENSE,
                LocalDate.now(), Money.won(4_000), categoryFood.getId(), "간식");
        assertEquals(96_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());

        // 게시 상태에서 바로 삭제 시도 → 실패(도메인 예외)
        try {
            transactionAppService.delete(tx.getId());
            fail("게시 상태 삭제는 금지되어야 합니다.");
        } catch (DomainException expected) { /* OK */ }

        // unpost → 잔액 원복 → 삭제
        transactionAppService.oneClickDelete(tx.getId());
        assertEquals(100_000L, accountAppService.getDetails(account.getId()).getCurrentBalance().toLong());
        assertFalse(transactionRepository.findById(tx.getId()).isPresent());
    }
    
    
    private Transaction createPosted(LocalDate date, long won, String memo) {
        return transactionAppService.oneClickCreate(
                ledgerId, account.getId(), TransactionType.EXPENSE,
                date, Money.won(won), categoryFood.getId(), memo
        );
    }
    
	@Test
	public void listByLedgerAndPeriod_filtersAndPaginates() {
		// given: 범위 밖/안으로 여러 건 생성(모두 게시)
		LocalDate base = LocalDate.now();
		createPosted(base.minusDays(6), 500, "out-6"); // 범위 밖
		createPosted(base.minusDays(4), 1000, "in-4"); // 범위 안
		createPosted(base.minusDays(3), 2000, "in-3"); // 범위 안
		createPosted(base.minusDays(2), 3000, "in-2"); // 범위 안
		createPosted(base.minusDays(1), 4000, "in-1"); // 범위 안
		createPosted(base, 600, "out-0"); // 범위 밖

		LocalDate from = base.minusDays(4);
		LocalDate to = base.minusDays(1);

		// when: 전체, 페이지1, 페이지2 조회
		List<Transaction> allInRange = transactionAppService.listByLedgerAndPeriod(ledgerId, from, to, 10, 0);
		List<Transaction> page1 = transactionAppService.listByLedgerAndPeriod(ledgerId, from, to, 2, 0);
		List<Transaction> page2 = transactionAppService.listByLedgerAndPeriod(ledgerId, from, to, 2, 2);

		// then: 총 4건이 범위 내
		assertEquals(4, allInRange.size());

		// 모두 기간 필터를 만족
		assertTrue(allInRange.stream().allMatch(tx -> !tx.getDate().isBefore(from) && !tx.getDate().isAfter(to)));
		assertTrue(page1.stream().allMatch(tx -> !tx.getDate().isBefore(from) && !tx.getDate().isAfter(to)));
		assertTrue(page2.stream().allMatch(tx -> !tx.getDate().isBefore(from) && !tx.getDate().isAfter(to)));

		// 페이지 크기 준수
		assertTrue(page1.size() <= 2);
		assertTrue(page2.size() <= 2);

		// 페이징 결과 합집합 == 전체 결과 (중복/누락 없음)
		Set<TransactionId> unionIds = Stream.concat(page1.stream(), page2.stream()).map(Transaction::getId)
				.collect(Collectors.toSet());
		Set<TransactionId> allIds = allInRange.stream().map(Transaction::getId).collect(Collectors.toSet());
		assertEquals(allIds, unionIds);
	}
}
