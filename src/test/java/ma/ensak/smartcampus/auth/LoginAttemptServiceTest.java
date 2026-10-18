package ma.ensak.smartcampus.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ma.ensak.smartcampus.user.Role;
import ma.ensak.smartcampus.user.User;
import ma.ensak.smartcampus.user.UserRepository;

/** Test unitaire isolé (sans Spring ni base de données). */
@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private SecurityEventRepository securityEvents;

    private LoginAttemptService loginAttempts;
    private User user;

    @BeforeEach
    void setUp() {
        loginAttempts = new LoginAttemptService(users, securityEvents);
        user = new User("etudiant@usms.ac.ma", "$2a$hash", Role.ETUDIANT, true);
    }

    @Test
    void sousLeSeuil_pasDeJournalisation() {
        assertThat(loginAttempts.registerFailure(user, "10.0.0.1")).isFalse();
        assertThat(loginAttempts.registerFailure(user, "10.0.0.1")).isFalse();

        assertThat(user.getFailedAttempts()).isEqualTo(2);
        verify(securityEvents, never()).save(any());
    }

    @Test
    void seuilAtteint_evenementJournaliseAvecIp() {
        for (int i = 1; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            loginAttempts.registerFailure(user, "10.0.0.1");
        }
        assertThat(loginAttempts.registerFailure(user, "10.0.0.1")).isTrue();

        ArgumentCaptor<SecurityEvent> event = ArgumentCaptor.forClass(SecurityEvent.class);
        verify(securityEvents).save(event.capture());
        assertThat(event.getValue().getEmail()).isEqualTo("etudiant@usms.ac.ma");
        assertThat(event.getValue().getIpAddress()).isEqualTo("10.0.0.1");
    }

    @Test
    void succes_remetLeCompteurAZeroEtDateLaConnexion() {
        loginAttempts.registerFailure(user, "10.0.0.1");
        loginAttempts.registerSuccess(user);

        assertThat(user.getFailedAttempts()).isZero();
        assertThat(user.getLastLoginAt()).isNotNull();
    }
}
