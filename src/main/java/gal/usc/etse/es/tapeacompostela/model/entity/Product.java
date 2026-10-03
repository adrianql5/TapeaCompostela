package gal.usc.etse.es.tapeacompostela.model.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uq_products", columnNames = {"business_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    @NotNull
    private Business business;

    @Column(nullable = false, length = 150)
    @NotBlank
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 6, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private Category category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Builder.Default
    private boolean available = true;

    @ManyToMany
    @JoinTable(name = "product_allergens",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id"))
    @Builder.Default
    private Set<Allergen> allergens = new HashSet<>();

    public enum Category {
        TAPA, PORTION, PINCHO, DRINK, DESSERT
    }
}
