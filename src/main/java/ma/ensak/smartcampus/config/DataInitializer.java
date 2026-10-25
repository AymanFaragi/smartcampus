package ma.ensak.smartcampus.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ma.ensak.smartcampus.user.Role;
import ma.ensak.smartcampus.user.User;
import ma.ensak.smartcampus.user.UserRepository;

/**
 * Comptes de démonstration (base H2 en mémoire).
 * Créés pendant le démarrage du contexte, donc avant que le serveur web n'accepte des requêtes.
 */
@Component
public class DataInitializer implements InitializingBean {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void afterPropertiesSet() {
        if (users.count() > 0) {
            return;
        }
        users.save(new User("etudiant@usms.ac.ma", passwordEncoder.encode("Secret123"), Role.ETUDIANT, true));
        users.save(new User("enseignant@usms.ac.ma", passwordEncoder.encode("Campus2026"), Role.ENSEIGNANT, true));
        users.save(new User("admin@usms.ac.ma", passwordEncoder.encode("Admin2026"), Role.ADMIN, true));
        users.save(new User("ancien@usms.ac.ma", passwordEncoder.encode("Secret123"), Role.ETUDIANT, false));
    }
}
