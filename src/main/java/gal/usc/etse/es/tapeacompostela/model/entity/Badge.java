package gal.usc.etse.es.tapeacompostela.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Insignia. Se consigue cuando el valor de conditionType llega a targetValue
 * (p. ej. REVIEW_COUNT >= 10).
 */
@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank
    private String code;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 30)
    @NotNull
    private ConditionType conditionType;

    @Column(name = "target_value", nullable = false)
    @NotNull
    @Positive
    private Integer targetValue;

    public enum ConditionType {
        REVIEW_COUNT, BUSINESSES_REVIEWED, POINTS_EARNED
    }
}
