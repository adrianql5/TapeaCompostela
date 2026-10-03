package gal.usc.etse.es.tapeacompostela.model.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.*;

/**
 * Franja horaria de un local. Si closesAt < opensAt, la franja cruza la medianoche.
 */
@Entity
@Table(name = "opening_hours",
        uniqueConstraints = @UniqueConstraint(name = "uq_opening_hours",
                columnNames = {"business_id", "day_of_week", "opens_at"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Convert(converter = DayOfWeekConverter.class)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "opens_at", nullable = false)
    private LocalTime opensAt;

    @Column(name = "closes_at", nullable = false)
    private LocalTime closesAt;

    /**
     * Guarda DayOfWeek como 1 (lunes) ... 7 (domingo), igual que la tabla.
     * Con @Enumerated(ORDINAL) se guardaría 0..6 y fallaría el CHECK.
     */
    @Converter
    public static class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Integer> {

        @Override
        public Integer convertToDatabaseColumn(DayOfWeek day) {
            return day == null ? null : day.getValue();
        }

        @Override
        public DayOfWeek convertToEntityAttribute(Integer value) {
            return value == null ? null : DayOfWeek.of(value);
        }
    }
}
