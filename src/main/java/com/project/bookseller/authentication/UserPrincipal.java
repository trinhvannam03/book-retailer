package com.project.bookseller.authentication;

import com.project.bookseller.entity.user.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserPrincipal {
    private User user;
    public Long getUserId() {
        return user.getUserId();
    }
    public String getEmail() {
        return user.getEmail();
    }

    public String getUserTier() {
        return user.getUserTier().toString();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public String getPhone() {
        return user.getPhone();
    }

    public String getProfilePicture() {
        return user.getProfilePicture();
    }

    public String getPasswordHash() {
        return user.getPasswordHash();
    }

    private List<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.user = user;
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleName()));
        authorities.add(new SimpleGrantedAuthority("TIER_" + user.getUserTier().toString()));
        this.authorities = authorities;
    }
}
