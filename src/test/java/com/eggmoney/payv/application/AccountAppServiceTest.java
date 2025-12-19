package com.eggmoney.payv.application;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.application.service.AccountAppService;
import com.eggmoney.payv.application.service.LedgerAppService;
import com.eggmoney.payv.application.service.UserAppService;
import com.eggmoney.payv.domain.model.entity.Account;
import com.eggmoney.payv.domain.model.entity.AccountType;
import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.Money;

import static org.junit.Assert.*;

/**
 * Account Application Service Test Class
 * @author 정의탁
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/root-context.xml")
@Transactional
public class AccountAppServiceTest {

	@Resource UserAppService userAppService;
    @Resource LedgerAppService ledgerAppService;
    @Resource AccountAppService accountAppService;

    private LedgerId ledgerId;

	private static String email() {
		return "u_" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
	}

	private static String ledgerName() {
		return "가계부_" + UUID.randomUUID().toString().substring(0, 8);
	}

	private static String accountName() {
		return "자산_" + UUID.randomUUID().toString().substring(0, 6);
	}

    @Before
    public void setUp() {
        User owner = userAppService.register(email(), "{noop}pw", "소유자");
        Ledger ledger = ledgerAppService.createLedger(owner.getId(), ledgerName());
        ledgerId = ledger.getId();
    }

    @Test
    public void createAccount_success() {
        Account acc = accountAppService.createAccount(ledgerId, AccountType.CASH, accountName(), Money.won(100_000));
        
        assertNotNull(acc.getId());
        assertEquals(100_000L, acc.getCurrentBalance().toLong());
    }

    @Test
    public void deposit_increasesBalance() {
        Account acc = accountAppService.createAccount(ledgerId, AccountType.CASH, accountName(), Money.won(100_000));
        
        accountAppService.deposit(acc.getId(), ledgerId, Money.won(10_000));
        
        assertEquals(110_000L, accountAppService.getDetails(acc.getId()).getCurrentBalance().toLong());
    }

    @Test
    public void withdraw_decreasesBalance() {
        Account acc = accountAppService.createAccount(ledgerId, AccountType.CASH, accountName(), Money.won(100_000));

        accountAppService.withdraw(acc.getId(), ledgerId, Money.won(5_000));
        
        assertEquals(95_000L, accountAppService.getDetails(acc.getId()).getCurrentBalance().toLong());
    }

    @Test
    public void rename_updatesName() {
        Account acc = accountAppService.createAccount(ledgerId, AccountType.CASH, accountName(), Money.zero());
        
        accountAppService.rename(acc.getId(), ledgerId, "지갑A");
        
        assertEquals("지갑A", accountAppService.getDetails(acc.getId()).getName());
    }

    @Test
    public void archive_thenReopen_togglesState() {
        Account acc = accountAppService.createAccount(ledgerId, AccountType.CASH, accountName(), Money.zero());
        
        accountAppService.archive(acc.getId(), ledgerId);
        assertTrue(accountAppService.getDetails(acc.getId()).isArchived());
        
        accountAppService.reopen(acc.getId(), ledgerId);
        assertFalse(accountAppService.getDetails(acc.getId()).isArchived());
    }

    @Test
    public void listByLedger_containsOpenedAccounts() {
        Account a = accountAppService.createAccount(ledgerId, AccountType.CASH, accountName(), Money.zero());
        Account b = accountAppService.createAccount(ledgerId, AccountType.BANK, accountName(), Money.zero());

        List<Account> list = accountAppService.listByLedger(ledgerId);
        
        assertTrue(list.stream().anyMatch(x -> x.getId().equals(a.getId())));
        assertTrue(list.stream().anyMatch(x -> x.getId().equals(b.getId())));
    }
}
