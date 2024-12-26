package com.project.bookseller.authentication;

import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Data
public class JwtAuthenticationToken implements Authentication {

    private Object principal;
    private Object credentials;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isAuthenticated = false;

    public JwtAuthenticationToken(Object principal) {
        this.principal = principal;
        this.authorities = ((UserDetails) principal).getAuthorities();
        setAuthenticated(true);
    }

    public JwtAuthenticationToken() {
        setAuthenticated(false);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return ((UserDetails) principal).getEmail();
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return null;
    }
}
