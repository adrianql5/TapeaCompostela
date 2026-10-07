package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotNull;

public record MeetupStop(
        @NotNull Long businessId,
        // Starts at 1. Ignored in requests: the order of the list is used instead
        Integer stopOrder
) {
    public static MeetupStop from(gal.usc.etse.es.tapeacompostela.model.entity.MeetupStop stop) {
        return new MeetupStop(stop.getBusiness().getId(), stop.getStopOrder());
    }
}
