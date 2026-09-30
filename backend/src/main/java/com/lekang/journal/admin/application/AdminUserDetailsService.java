package com.lekang.journal.admin.application;

import com.lekang.journal.admin.infrastructure.AdminUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserDetailsService implements UserDetailsService {
    private final AdminUserRepository repository;

    public AdminUserDetailsService(AdminUserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        var admin = repository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("administrator not found"));
        return new User(
            admin.getUsername(),
            admin.getPasswordHash(),
            admin.isEnabled(),
            true,
            true,
            true,
            java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
