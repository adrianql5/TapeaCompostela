package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record OpeningHour(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime opensAt,
        @NotNull LocalTime closesAt
) {
    public static OpeningHour from(gal.usc.etse.es.tapeacompostela.model.entity.OpeningHour hour) {
        return new OpeningHour(hour.getDayOfWeek(), hour.getOpensAt(), hour.getClosesAt());
    }
}
