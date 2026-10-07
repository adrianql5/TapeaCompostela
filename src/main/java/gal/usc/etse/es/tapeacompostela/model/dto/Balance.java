package gal.usc.etse.es.tapeacompostela.model.dto;

// Points that a user has available in one business
public record Balance(
        Long businessId,
        Integer points
) {}
