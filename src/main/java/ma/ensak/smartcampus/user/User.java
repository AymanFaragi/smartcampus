package ma.ensak.smartcampus.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int failedAttempts;

    private LocalDateTime lastLoginAt;

    protected User() {
    }

    public User(String email, String passwordHash, Role role, boolean active) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /** Ajoute un échec de connexion et renvoie le nombre d'échecs consécutifs. */
    public int registerFailedAttempt() {
        return ++failedAttempts;
    }

    /** Connexion réussie : remet le compteur d'échecs à zéro et date la connexion. */
    public void registerSuccessfulLogin(LocalDateTime at) {
        failedAttempts = 0;
        lastLoginAt = at;
    }
}
