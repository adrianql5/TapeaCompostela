package gal.usc.etse.es.tapeacompostela.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

// Un token opaco que permite al cliente obtener un nuevo JWT sin la contraseña
// Se usa unca vez: cuando se usa o cuando el usuario hace log out, se marca como revocado
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;
}