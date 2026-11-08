package gal.usc.etse.es.tapeacompostela.model.dto;

import java.util.List;

// Un negocio con sus ultimas reseñas y recompensas en un una respuesta
public record BusinessSummaryDTO(BusinessDTO business, List<ReviewDTO> latestReviews, List<RewardDTO> rewards) {}