package gal.usc.etse.es.tapeacompostela.model.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Movimiento de puntos de un usuario en un local. Saldo = SUM(points).
 * EARN: points > 0 y registeredBy obligatorio. REDEEM: points < 0 y redemption obligatorio.
 */
@Entity
@Table(name = "point_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    @NotNull
    private Business business;

    @Column(nullable = false)
    @NotNull
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private Type type;

    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_id")
    private User registeredBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redemption_id", unique = true)
    private Redemption redemption;

    @AssertTrue(message = "EARN needs points > 0 and registeredBy; REDEEM needs points < 0 and redemption")
    private boolean isConsistentWithType() {
        if (type == null || points == null) {
            return true;
        }
        return switch (type) {
            case EARN -> points > 0 && registeredBy != null;
            case REDEEM -> points < 0 && redemption != null;
        };
    }

    public enum Type {
        EARN, REDEEM
    }
}
