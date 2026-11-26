package gal.usc.etse.es.tapeacompostela.event;

// Algo que ya ha ocurrido: un usuario escribió una reseña.
// Un record, porque un evento no cambia
public record ReviewCreatedEvent(Long reviewId, String username, Long businessId) {}