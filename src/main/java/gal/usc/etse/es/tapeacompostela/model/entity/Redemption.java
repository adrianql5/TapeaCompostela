package gal.usc.etse.es.tapeacompostela.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Una recompensa comprado con puntos. El usuario enseña el codigo en el negocio
// y el propietario lo marca como canjeado.
@Entity
@Table(name = "redemptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Redemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private LocalDateTime redeemedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public enum Status {
        PENDING, USED, EXPIRED
    }
}