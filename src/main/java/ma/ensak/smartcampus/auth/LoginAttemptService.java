package ma.ensak.smartcampus.auth;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ma.ensak.smartcampus.user.User;
import ma.ensak.smartcampus.user.UserRepository;

/** Gestion des tentatives de connexion : compteur d'échecs et journal de sécurité. */
@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 3;
    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final UserRepository users;
    private final SecurityEventRepository securityEvents;

    public LoginAttemptService(UserRepository users, SecurityEventRepository securityEvents) {
        this.users = users;
        this.securityEvents = securityEvents;
    }

    /**
     * Enregistre un échec de connexion.
     * @return true si le seuil est atteint et que l'événement a été journalisé
     */
    public boolean registerFailure(User user, String ipAddress) {
        int failedAttempts = user.registerFailedAttempt();
        users.save(user);
        if (failedAttempts < MAX_ATTEMPTS) {
            return false;
        }
        securityEvents.save(new SecurityEvent(LocalDateTime.now(), user.getEmail(), ipAddress));
        log.warn("[SECURITY] {} échecs consécutifs pour {} depuis {}", failedAttempts, user.getEmail(), ipAddress);
        return true;
    }

    /** Connexion réussie : remet le compteur à zéro et date la connexion. */
    public void registerSuccess(User user) {
        user.registerSuccessfulLogin(LocalDateTime.now());
        users.save(user);
    }
}
