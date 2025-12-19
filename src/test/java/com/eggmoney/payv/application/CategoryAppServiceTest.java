package com.eggmoney.payv.application;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.application.service.CategoryAppService;
import com.eggmoney.payv.application.service.LedgerAppService;
import com.eggmoney.payv.application.service.UserAppService;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.entity.Ledger;
import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.model.repository.CategoryRepository;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.domain.shared.error.DomainException;

/**
 * Category Application Service Test Class
 * @author 정의탁
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/root-context.xml")
@Transactional
public class CategoryAppServiceTest {

	@Resource UserAppService userAppService;
    @Resource LedgerAppService ledgerAppService;
    @Resource CategoryAppService categoryAppService;
    @Resource CategoryRepository categoryRepository;

    private User owner;
    private Ledger ledger;          // 기본 테스트용 가계부
    private Ledger otherLedger;     // 교차-가계부 검증용

	private static String email() {
		return "u_" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
	}

	private static String uniqueLedgerName() {
		return "ledger_" + UUID.randomUUID().toString().substring(0, 8);
	}

    @Before
    public void setUp() {
        owner = userAppService.register(email(), "{noop}pw", "소유자");
        UserId ownerId = owner.getId();
        ledger = ledgerAppService.createLedger(ownerId, uniqueLedgerName());
        otherLedger = ledgerAppService.createLedger(ownerId, uniqueLedgerName());
    }

    @Test
    public void createRoot_success() {
        Category root = categoryAppService.createRoot(ledger.getId(), "식비", false, 0);
        assertNotNull(root.getId());
        assertTrue(root.isRoot());
        assertEquals("식비", root.getName());
    }
    
    @Test(expected = DomainException.class)
    public void createRoot_duplicateNameFailsWithinSameLedger() {
        categoryAppService.createRoot(ledger.getId(), "식비", false, 0);
        categoryAppService.createRoot(ledger.getId(), "식비", false, 1); // 동일 가계부 + 동일 이름 → 실패
    }
    
    @Test
    public void createRoot_sameNameAllowedInDifferentLedger() {
        categoryAppService.createRoot(ledger.getId(), "식비", false, 0);
        Category other = categoryAppService.createRoot(otherLedger.getId(), "식비", false, 0);
        assertNotNull(other.getId());
    }

    @Test
    public void createChild_underRoot_success() {
        Category parent = categoryAppService.createRoot(ledger.getId(), "교통", false, 0);
        Category child  = categoryAppService.createChild(ledger.getId(), parent.getId(), "버스", false, 0);
        assertFalse(child.isRoot());
        assertEquals(parent.getId(), child.getParentId());
        assertEquals("버스", categoryRepository.findById(child.getId())
        		.orElseThrow(AssertionError::new).getName());
    }

    @Test(expected = DomainException.class)
    public void createChild_depthGreaterThan2_forbidden() {
        Category root = categoryAppService.createRoot(ledger.getId(), "패션/잡화", false, 0);
        Category child = categoryAppService.createChild(ledger.getId(), root.getId(), "신발", false, 0);
        // 카테고리 3-Depth 이상 생성 시도 → 실패
        categoryAppService.createChild(ledger.getId(), child.getId(), "운동화", false, 0);
    }

    @Test(expected = DomainException.class)
    public void createChild_parentLedgerMismatch_forbidden() {
        Category parent = categoryAppService.createRoot(ledger.getId(), "기타", false, 0);
        // 부모는 ledger, 자식은 otherLedger에 생성 요청 → 금지
        categoryAppService.createChild(otherLedger.getId(), parent.getId(), "기타-자식", false, 0);
    }

    @Test
    public void rename_success() {
        Category c = categoryAppService.createRoot(ledger.getId(), "여가", false, 1);
        categoryAppService.rename(c.getId(), ledger.getId(), "레저");
        assertEquals("레저", categoryRepository.findById(c.getId())
        		.orElseThrow(AssertionError::new).getName());
    }

    @Test(expected = DomainException.class)
    public void rename_duplicateNameForbidden() {
        Category a = categoryAppService.createRoot(ledger.getId(), "취미", false, 0);
        Category b = categoryAppService.createRoot(ledger.getId(), "여가", false, 1);
        categoryAppService.rename(b.getId(), ledger.getId(), "취미"); // 동일 가계부 내 중복 이름으로 rename → 실패
    }

    @Test(expected = DomainException.class)
    public void rename_ledgerMismatch_forbidden() {
        Category a = categoryAppService.createRoot(ledger.getId(), "가계", false, 0);
        categoryAppService.rename(a.getId(), otherLedger.getId(), "아무이름"); // 다른 ledgerId → 실패
    }
    
    @Test
    public void listByLedger() {
    	Category r = categoryAppService.createRoot(ledger.getId(), "패션", false, 0);
    	Category ca = categoryAppService.createRoot(ledger.getId(), "상의", false, 0);
    	Category cb = categoryAppService.createRoot(ledger.getId(), "하의", false, 0);
    	
    	List<Category> list = categoryAppService.listByLedger(ledger.getId());
    	
    	assertTrue(list.stream().anyMatch(x -> x.getId().equals(r.getId())));
        assertTrue(list.stream().anyMatch(x -> x.getId().equals(ca.getId())));
        assertTrue(list.stream().anyMatch(x -> x.getId().equals(cb.getId())));
    }
    
    @Test
    public void softDelete_thenSameNameCanBeRecreated_and_listSkipsDeleted() {
        Category food = categoryAppService.createRoot(ledger.getId(), "식비", false, 0);
        categoryAppService.delete(ledger.getId(), food.getId());

        // 동일 이름 재생성 (활성 유니크 기준에서는 허용)
        Category food2 = categoryAppService.createRoot(ledger.getId(), "식비", false, 0);
        assertNotEquals(food.getId(), food2.getId());

        // 목록에는 새 것만 보이고, 삭제된 것은 보이지 않음
        List<Category> list = categoryAppService.listByLedger(ledger.getId());
        assertTrue(list.stream().anyMatch(x -> x.getId().equals(food2.getId())));
        assertFalse(list.stream().anyMatch(x -> x.getId().equals(food.getId())));
    }
}
