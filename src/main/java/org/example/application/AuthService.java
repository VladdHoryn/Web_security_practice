package org.example.application;

import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.example.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Перевіряє логін/пароль з БД і видає JWT.
     * На неіснуючий логін і неправильний пароль відповідь однакова (401),
     * щоб не можна було перебором дізнатися, які логіни існують.
     */
    public TokenResponse login(String username, String password) {
        if (username == null || password == null) {
            throw invalidCredentials();
        }
        Account account = accountRepository.findByUsername(username)
                .filter(a -> passwordEncoder.matches(password, a.getPassword()))
                .orElseThrow(AuthService::invalidCredentials);

        return issueToken(account);
    }

    /**
     * Реєстрація нового облікового запису. Завжди з роллю USER —
     * роль не можна передати з клієнта.
     */
    @Transactional
    public TokenResponse register(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Логін обов'язковий, пароль — щонайменше 8 символів");
        }
        if (accountRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Користувач з таким логіном вже існує");
        }
        Account account = accountRepository.save(
                new Account(null, username, passwordEncoder.encode(password), Account.Role.USER));
        return issueToken(account);
    }

    private TokenResponse issueToken(Account account) {
        String token = jwtService.generateToken(account.getUsername(), account.getRole().name());
        return new TokenResponse(token, "Bearer", jwtService.getExpirationSeconds(), account.getRole().name());
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неправильний логін або пароль");
    }

    public record TokenResponse(String token, String tokenType, long expiresIn, String role) {
    }
}
