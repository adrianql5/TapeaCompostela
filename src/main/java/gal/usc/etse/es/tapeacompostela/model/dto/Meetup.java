package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "meetups")
public record Meetup(
        Long id,
        UserSummary creator,
        @NotBlank String title,
        String description,
        @NotNull LocalDateTime scheduledAt,
        LocalDateTime createdAt
) {
    public static Meetup from(gal.usc.etse.es.tapeacompostela.model.entity.Meetup meetup) {
        return new Meetup(
                meetup.getId(),
                UserSummary.from(meetup.getCreator()),
                meetup.getTitle(),
                meetup.getDescription(),
                meetup.getScheduledAt(),
                meetup.getCreatedAt()
        );
    }
}
