package com.eggmoney.payv.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.application.service.LedgerAppService;
import com.eggmoney.payv.application.service.UserAppService;
import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.domain.shared.error.DomainException;

/**
 * Ledger Application Service Test Class
 * @author 정의탁
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/root-context.xml")
@Transactional
public class LedgerAppServiceTest {

	@Resource 
	UserAppService userAppService;
	
    @Resource 
    LedgerAppService ledgerAppService;

    private UserId ownerId;
    
	private static String email() {
		return "u_" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
	}

	private static String uniqueLedgerName() {
		return "가계부_" + UUID.randomUUID().toString().substring(0, 8);
	}
    
    @Before
    public void setUp() {
        User owner = userAppService.register(email(), "{noop}pw", "소유자");
        ownerId = owner.getId();
    }

    @Test
    public void create_success_and_duplicateNameFails() {

    	// 가계부 생성
        String name = uniqueLedgerName();
        Ledger ledger = ledgerAppService.createLedger(ownerId, name);
        
        assertNotNull(ledger);
        assertNotNull(ledger.getId());
        assertEquals(ownerId, ledger.getOwnerId());
        assertEquals(name, ledger.getName());

        // 동일 이름 재생성 → LEDGER.NAME UNIQUE 위반
        try {
            ledgerAppService.createLedger(ownerId, name);
            fail("Expected duplicate ledger name failure");
        } catch (DomainException | org.springframework.dao.DuplicateKeyException expected) {
            // OK
        }
    }

    @Test
    public void rename_success_and_reflectsChange() {
        Ledger ledger = ledgerAppService.createLedger(ownerId, uniqueLedgerName());
        LedgerId ledgerId = ledger.getId();

        String newName = uniqueLedgerName();
        ledgerAppService.rename(ledgerId, newName, ownerId);
        
        try {
            ledgerAppService.createLedger(ownerId, newName); // 같은 이름 다시 만들면 UNIQUE 위반
            fail("Expected duplicate after rename");
        } catch (DomainException | org.springframework.dao.DuplicateKeyException expected) {
            // OK
        }
    }
    
    @Test
    public void listByOwner() {
    	Ledger a = ledgerAppService.createLedger(ownerId, uniqueLedgerName());
    	Ledger b = ledgerAppService.createLedger(ownerId, uniqueLedgerName());
    	
    	List<Ledger> list = ledgerAppService.listByOwner(ownerId);
    	
    	assertTrue(list.stream().anyMatch(x -> x.getId().equals(a.getId())));
        assertTrue(list.stream().anyMatch(x -> x.getId().equals(b.getId())));
    }
}
