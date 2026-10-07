package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "comments")
public record MeetupComment(
        Long id,
        Long meetupId,
        UserSummary author,
        @NotBlank String content,
        LocalDateTime createdAt
) {
    public static MeetupComment from(gal.usc.etse.es.tapeacompostela.model.entity.MeetupComment comment) {
        return new MeetupComment(
                comment.getId(),
                comment.getMeetup().getId(),
                UserSummary.from(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
