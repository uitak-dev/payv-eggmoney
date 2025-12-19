package com.eggmoney.payv.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.eggmoney.payv.domain.model.vo.UserId;

import lombok.Data;

@Data
public class CustomUser extends User {

    private UserId userId;
    private String email;

    public CustomUser() {
        super("anonymous", "", Collections.emptyList());
    }

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public CustomUser(com.eggmoney.payv.domain.model.entity.User user) {
        super(user.getEmail(), user.getPassword(),
              Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.userId = user.getId();
        this.email = user.getEmail();
    }
}
