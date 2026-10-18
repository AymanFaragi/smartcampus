package ma.ensak.smartcampus.auth;

import java.time.LocalDateTime;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensak.smartcampus.user.User;
import ma.ensak.smartcampus.user.UserRepository;

@Service
public class LoginService {

    public static final String ERROR_MESSAGE = "E-mail ou mot de passe incorrect";
    private static final String DOMAIN = "@usms.ac.ma";
    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final SecurityEventRepository securityEvents;

    public LoginService(UserRepository users, PasswordEncoder passwordEncoder,
                        SecurityEventRepository securityEvents) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.securityEvents = securityEvents;
    }

    public LoginResult authenticate(String email, String password) {
        return authenticate(email, password, null);
    }

    @Transactional
    public LoginResult authenticate(String email, String password, String ipAddress) {
        if (email == null || !email.toLowerCase(Locale.ROOT).endsWith(DOMAIN)) {
            return LoginResult.INVALID_EMAIL_FORMAT;
        }

        User user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return LoginResult.INVALID_CREDENTIALS;
        }
        if (!user.isActive()) {
            return LoginResult.INACTIVE_ACCOUNT;
        }

        if (password != null && passwordEncoder.matches(password, user.getPasswordHash())) {
            user.registerSuccessfulLogin(LocalDateTime.now());
            users.save(user);
            return LoginResult.SUCCESS;
        }

        int failedAttempts = user.registerFailedAttempt();
        users.save(user);
        if (failedAttempts >= 3) {
            securityEvents.save(new SecurityEvent(LocalDateTime.now(), user.getEmail(), ipAddress));
            log.warn("[SECURITY] 3 échecs consécutifs pour {} depuis {}", user.getEmail(), ipAddress);
            return LoginResult.LOCKED_AND_LOGGED;
        }
        return LoginResult.INVALID_CREDENTIALS;
    }
}
