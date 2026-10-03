package gal.usc.etse.es.tapeacompostela.model.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "rewards",
        uniqueConstraints = @UniqueConstraint(name = "uq_rewards", columnNames = {"business_id", "title"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    @NotNull
    private Business business;

    @Column(nullable = false, length = 150)
    @NotBlank
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "points_cost", nullable = false)
    @NotNull
    @Positive
    private Integer pointsCost;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "start_date", nullable = false)
    @Builder.Default
    @NotNull
    private LocalDate startDate = LocalDate.now();

    // null = sin fecha de fin
    @Column(name = "end_date")
    private LocalDate endDate;

    @AssertTrue(message = "endDate must not be before startDate")
    private boolean isEndDateValid() {
        return endDate == null || startDate == null || !endDate.isBefore(startDate);
    }
}
