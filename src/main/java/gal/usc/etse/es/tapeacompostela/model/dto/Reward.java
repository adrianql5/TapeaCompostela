package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;

@Relation(collectionRelation = "rewards")
public record Reward(
        Long id,
        Long businessId,
        @NotBlank String title,
        String description,
        @NotNull @Positive Integer pointsCost,
        Boolean active,
        LocalDate startDate,
        // null means the reward never expires
        LocalDate endDate
) {
    public static Reward from(gal.usc.etse.es.tapeacompostela.model.entity.Reward reward) {
        return new Reward(
                reward.getId(),
                reward.getBusiness().getId(),
                reward.getTitle(),
                reward.getDescription(),
                reward.getPointsCost(),
                reward.isActive(),
                reward.getStartDate(),
                reward.getEndDate()
        );
    }
}
