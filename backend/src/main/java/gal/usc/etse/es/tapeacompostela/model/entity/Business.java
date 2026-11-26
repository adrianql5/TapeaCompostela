package gal.usc.etse.es.tapeacompostela.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hibernate comprueba esto antes de guardar, así un PATCH no puede dejar un negocio sin datos obligatorios
    @Column(nullable = false, length = 150)
    @NotBlank
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    @NotBlank
    private String address;

    @Column(nullable = false)
    @NotNull
    private Double latitude;

    @Column(nullable = false)
    @NotNull
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_range", length = 10)
    private PriceRange priceRange;

    @Column(length = 20)
    private String phone;

    // Calculado por el servidor a partir de las reseñas, el usuario nunca lo manda
    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    @ColumnDefault("0")
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Integer ratingCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // La tabla de negocios tiene la columna owner_id, con LAZY el owner solo se carga si se usa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToMany
    @JoinTable(name = "business_tags",
            joinColumns = @JoinColumn(name = "business_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // Las reseñas y recompensas del negocio. Si se borra el negocio se borran también (en cascada)
    // y al quitar una de la lista se borra de la base de datos (orphanRemoval)
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reward> rewards = new ArrayList<>();

    public enum PriceRange {
        CHEAP, MODERATE, EXPENSIVE
    }
}