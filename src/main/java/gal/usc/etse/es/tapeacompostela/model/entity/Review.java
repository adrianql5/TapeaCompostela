package gal.usc.etse.es.tapeacompostela.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Reseña de un local (product == null) o de un producto de ese local (product != null).
 * That the product belongs to the business is checked by isProductFromBusiness().
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Column(length = 2000)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Respuesta del propietario: reply y repliedAt van siempre juntos
    @Column(length = 2000)
    private String reply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @AssertTrue(message = "product must belong to the reviewed business")
    private boolean isProductFromBusiness() {
        return product == null || business == null
                || Objects.equals(product.getBusiness().getId(), business.getId());
    }

    @AssertTrue(message = "reply and repliedAt must be set together")
    private boolean isReplyComplete() {
        return (reply == null) == (repliedAt == null);
    }
}
