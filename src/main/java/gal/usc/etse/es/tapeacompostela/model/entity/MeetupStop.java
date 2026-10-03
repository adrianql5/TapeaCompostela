package gal.usc.etse.es.tapeacompostela.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meetup_stops", uniqueConstraints = {
        @UniqueConstraint(name = "uq_meetup_stops_order", columnNames = {"meetup_id", "stop_order"}),
        @UniqueConstraint(name = "uq_meetup_stops_business", columnNames = {"meetup_id", "business_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetupStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meetup_id", nullable = false)
    private Meetup meetup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    // Empieza en 1
    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;
}
