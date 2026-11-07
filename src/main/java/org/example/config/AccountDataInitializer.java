package org.example.config;

import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Під час старту створює в БД три стандартні облікові записи, якщо їх ще немає.
 * Паролі хешуються BCrypt-ом тут, а не зберігаються хешами в SQL-міграції.
 */
@Component
public class AccountDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AccountDataInitializer.class);

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountDataInitializer(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing("user", "User123!", Account.Role.USER);
        createIfMissing("librarian", "Librarian123!", Account.Role.LIBRARIAN);
        createIfMissing("admin", "Adminamdin123!", Account.Role.ADMIN);
    }

    private void createIfMissing(String username, String rawPassword, Account.Role role) {
        if (accountRepository.existsByUsername(username)) {
            return;
        }
        accountRepository.save(new Account(null, username, passwordEncoder.encode(rawPassword), role));
        log.info("Створено обліковий запис '{}' з роллю {}", username, role);
    }
}
