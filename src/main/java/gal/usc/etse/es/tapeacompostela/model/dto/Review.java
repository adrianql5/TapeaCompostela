package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "reviews")
public record Review(
        Long id,
        UserSummary user,
        Long businessId,
        // null when the review is about the business and not one of its products
        Long productId,
        @NotNull @Min(1) @Max(5) Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String reply,
        LocalDateTime repliedAt,
        Long likes
) {
    public static Review from(gal.usc.etse.es.tapeacompostela.model.entity.Review review, long likes) {
        return new Review(
                review.getId(),
                UserSummary.from(review.getUser()),
                review.getBusiness().getId(),
                review.getProduct() == null ? null : review.getProduct().getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                review.getReply(),
                review.getRepliedAt(),
                likes
        );
    }
}
