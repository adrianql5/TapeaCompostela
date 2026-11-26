package gal.usc.etse.es.tapeacompostela.model.dto;

import java.util.List;

// Una página de reseñas con paginación por cursor. nextCursor se manda en la siguiente llamada como ?cursor=...;
// es null cuando no hay más
public record ReviewCursorPageDTO(List<ReviewDTO> data, Long nextCursor) {}