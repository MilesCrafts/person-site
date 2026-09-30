package com.lekang.journal.admin.application;

import com.lekang.journal.admin.infrastructure.AdminUserEntity;
import com.lekang.journal.admin.infrastructure.AdminUserRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminBootstrap(
        AdminUserRepository repository,
        PasswordEncoder passwordEncoder,
        @Value("${journal.admin.bootstrap-username:}") String username,
        @Value("${journal.admin.bootstrap-password:}") String password
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() && password.isBlank()) {
            return;
        }
        if (username.isBlank() || password.length() < 12) {
            throw new IllegalStateException(
                "administrator bootstrap requires a username and a password of at least 12 characters"
            );
        }
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (repository.findByUsernameIgnoreCase(normalizedUsername).isEmpty()) {
            repository.save(new AdminUserEntity(
                normalizedUsername,
                passwordEncoder.encode(password),
                OffsetDateTime.now(ZoneOffset.UTC)
            ));
        }
    }
}
