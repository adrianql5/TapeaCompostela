package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "comments")
public record ReviewComment(
        Long id,
        Long reviewId,
        UserSummary author,
        @NotBlank String content,
        LocalDateTime createdAt
) {
    public static ReviewComment from(gal.usc.etse.es.tapeacompostela.model.entity.ReviewComment comment) {
        return new ReviewComment(
                comment.getId(),
                comment.getReview().getId(),
                UserSummary.from(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
