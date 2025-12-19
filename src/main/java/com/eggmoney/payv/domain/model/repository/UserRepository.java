package com.eggmoney.payv.domain.model.repository;

import java.util.Optional;

import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.model.vo.UserId;

/**
 * 사용자 레포지토리
 * @author 정의탁
 */
public interface UserRepository {
	Optional<User> findById(UserId id);
    Optional<User> findByEmail(String email);
    
    // UPSERT = 새 UUID면 insert, 아니면 update
    void save(User user);
    
}
