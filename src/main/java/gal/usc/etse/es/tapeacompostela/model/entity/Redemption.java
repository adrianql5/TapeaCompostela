package gal.usc.etse.es.tapeacompostela.model.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reward_id", nullable = false)
    @NotNull
    private Reward reward;

    // Código que se enseña en el local
    @Column(nullable = false, unique = true, length = 20)
    @NotBlank
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    @NotNull
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private LocalDateTime redeemedAt;

    // Obligatorio cuando status = USED
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @AssertTrue(message = "usedAt is required when status is USED")
    private boolean isUsedAtValid() {
        return status != Status.USED || usedAt != null;
    }

    public enum Status {
        PENDING, USED, EXPIRED
    }
}
