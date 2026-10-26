package ma.ensak.smartcampus.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import ma.ensak.smartcampus.user.Role;
import ma.ensak.smartcampus.user.User;
import ma.ensak.smartcampus.user.UserRepository;

@SpringBootTest
class LoginServiceTest {

    private static final String IP = "10.0.0.42";

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserRepository users;

    @Autowired
    private SecurityEventRepository securityEvents;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Crée un compte propre à chaque test pour que les compteurs d'échecs restent indépendants. */
    private String newAccount(boolean active) {
        String email = "etudiant-" + UUID.randomUUID() + "@usms.ac.ma";
        users.save(new User(email, passwordEncoder.encode("Secret123"), Role.ETUDIANT, active));
        return email;
    }

    private int failedAttempts(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow().getFailedAttempts();
    }

    @Test
    void identifiantsCorrects_connexionReussie() {
        String email = newAccount(true);
        assertThat(loginService.authenticate(email, "Secret123")).isEqualTo(LoginResult.SUCCESS);
    }

    @Test
    void motDePasseIncorrect_refuse() {
        String email = newAccount(true);
        assertThat(loginService.authenticate(email, "faux")).isEqualTo(LoginResult.INVALID_CREDENTIALS);
        assertThat(failedAttempts(email)).isEqualTo(1);
    }

    @Test
    void domaineNonInstitutionnel_refuse() {
        assertThat(loginService.authenticate("etudiant@gmail.com", "Secret123"))
                .isEqualTo(LoginResult.INVALID_EMAIL_FORMAT);
    }

    @Test
    void compteInactif_refuse() {
        String email = newAccount(false);
        assertThat(loginService.authenticate(email, "Secret123")).isEqualTo(LoginResult.INACTIVE_ACCOUNT);
    }

    @Test
    void motDePasseStockeSousFormeDeHash() {
        String email = newAccount(true);
        String hash = users.findByEmailIgnoreCase(email).orElseThrow().getPasswordHash();
        assertThat(hash).isNotEqualTo("Secret123").startsWith("$3");
    }

    @Test
    void troisEchecs_evenementJournalise() {
        String email = newAccount(true);
        loginService.authenticate(email, "x", IP);
        loginService.authenticate(email, "x", IP);
        assertThat(loginService.authenticate(email, "x", IP)).isEqualTo(LoginResult.LOCKED_AND_LOGGED);

        assertThat(securityEvents.findByEmailIgnoreCase(email))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getIpAddress()).isEqualTo(IP);
                    assertThat(event.getOccurredAt()).isNotNull();
                });
    }

    @Test
    void compteurRemisAZeroApresConnexionReussie() {
        String email = newAccount(true);
        loginService.authenticate(email, "x");
        loginService.authenticate(email, "x");
        assertThat(failedAttempts(email)).isEqualTo(2);

        assertThat(loginService.authenticate(email, "Secret123")).isEqualTo(LoginResult.SUCCESS);
        assertThat(failedAttempts(email)).isZero();
        assertThat(users.findByEmailIgnoreCase(email).orElseThrow().getLastLoginAt()).isNotNull();
    }

    @Test
    void compteDeTestDisponibleEnBase() {
        User user = users.findByEmailIgnoreCase("etudiant@usms.ac.ma").orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.ETUDIANT);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getPasswordHash()).startsWith("$2");
    }

    @Test
    void messageErreurConformeAuTicket() {
        assertThat(LoginService.ERROR_MESSAGE).isEqualTo("E-mail ou mot de passe incorrect");
    }
}
