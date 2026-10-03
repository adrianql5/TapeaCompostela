package gal.usc.etse.es.tapeacompostela.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

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

    // Un local adherido debe tener propietario
    @Column(name = "is_partner", nullable = false)
    @Builder.Default
    private boolean partner = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_range", length = 10)
    private PriceRange priceRange;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String website;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Las actualiza el servicio al crear/editar/borrar reseñas del local
    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    @ColumnDefault("0")
    @Builder.Default
    @NotNull
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    @NotNull
    @PositiveOrZero
    private Integer ratingCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(name = "business_tags",
            joinColumns = @JoinColumn(name = "business_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @AssertTrue(message = "a partner business must have an owner")
    private boolean isOwnerSetIfPartner() {
        return !partner || owner != null;
    }

    public enum PriceRange {
        CHEAP, MODERATE, EXPENSIVE
    }
}
