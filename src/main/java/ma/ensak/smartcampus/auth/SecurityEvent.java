package ma.ensak.smartcampus.auth;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Entrée du journal de sécurité : échecs de connexion répétés (date, e-mail, adresse IP). */
@Entity
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false)
    private String email;

    private String ipAddress;

    protected SecurityEvent() {
    }

    public SecurityEvent(LocalDateTime occurredAt, String email, String ipAddress) {
        this.occurredAt = occurredAt;
        this.email = email;
        this.ipAddress = ipAddress;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getEmail() {
        return email;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
