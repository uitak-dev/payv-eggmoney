package com.eggmoney.payv.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.eggmoney.payv.application.service.UserAppService;
import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.shared.error.DomainException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/root-context.xml")
// @Transactional
public class UserAppServiceTest {

	@Resource 
	UserAppService userAppService;

    private static String randomEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
    }

    @Test
    public void register_success_and_duplicateEmailFails() {
        String email = randomEmail();

        User u1 = userAppService.register(email, "{noop}pw", "테스터");
        assertNotNull(u1);
        assertNotNull(u1.getId());
        assertEquals(email, u1.getEmail());

        // 동일 이메일 재등록 → UNIQUE(EMAIL) 위반 = DomainException (또는 DuplicateKeyException)
        try {
            userAppService.register(email, "{noop}pw2", "테스터2");
            fail("Expected duplicate email failure");
        } catch (DomainException | org.springframework.dao.DuplicateKeyException expected) {
            // OK
        }
    }
}
