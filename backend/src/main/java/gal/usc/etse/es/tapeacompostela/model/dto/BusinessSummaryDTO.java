package gal.usc.etse.es.tapeacompostela.model.dto;

import java.util.List;

// Un negocio con sus últimas reseñas y sus recompensas en una sola respuesta (GET /businesses/{id}/summary)
public record BusinessSummaryDTO(BusinessDTO business, List<ReviewDTO> latestReviews, List<RewardDTO> rewards) {}