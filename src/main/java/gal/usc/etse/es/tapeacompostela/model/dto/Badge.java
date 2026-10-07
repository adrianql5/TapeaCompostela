package gal.usc.etse.es.tapeacompostela.model.dto;

import gal.usc.etse.es.tapeacompostela.model.entity.Badge.ConditionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record Badge(
        Long id,
        @NotBlank String code,
        @NotBlank String name,
        String description,
        String iconUrl,
        @NotNull ConditionType conditionType,
        @NotNull @Positive Integer targetValue
) {
    public static Badge from(gal.usc.etse.es.tapeacompostela.model.entity.Badge badge) {
        return new Badge(
                badge.getId(),
                badge.getCode(),
                badge.getName(),
                badge.getDescription(),
                badge.getIconUrl(),
                badge.getConditionType(),
                badge.getTargetValue()
        );
    }
}
