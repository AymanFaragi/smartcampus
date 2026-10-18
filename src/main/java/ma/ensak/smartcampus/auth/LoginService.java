package ma.ensak.smartcampus.auth;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ma.ensak.smartcampus.user.User;
import ma.ensak.smartcampus.user.UserRepository;

@Service
public class LoginService {

    public static final String ERROR_MESSAGE = "Mot de passe incorrect";
    private static final String DOMAIN = "@usms.ac.ma";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttempts;

    public LoginService(UserRepository users, PasswordEncoder passwordEncoder, LoginAttemptService loginAttempts) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.loginAttempts = loginAttempts;
    }

    public LoginResult authenticate(String email, String password) {
        return authenticate(email, password, null);
    }

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
            loginAttempts.registerSuccess(user);
            return LoginResult.SUCCESS;
        }

        boolean logged = loginAttempts.registerFailure(user, ipAddress);
        return logged ? LoginResult.LOCKED_AND_LOGGED : LoginResult.INVALID_CREDENTIALS;
    }
}
