package gal.usc.etse.es.tapeacompostela.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// Algo que el negocio a cambio de puntos, 
// por ejemplo una caña gratis por 30 puntos
@Entity
@Table(name = "rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "points_cost", nullable = false)
    private Integer pointsCost;

    // Una recompensa inactiva se guarda pero no se puede canjear
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Redemption> redemptions = new ArrayList<>();
}