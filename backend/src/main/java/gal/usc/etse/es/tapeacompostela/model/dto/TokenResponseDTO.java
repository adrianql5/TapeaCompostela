package gal.usc.etse.es.tapeacompostela.model.dto;

// accessToken: el JWT, se manda en "Authorization: Bearer ..." (válido 1 hora)
// refreshToken: se usa una vez en /auth/refresh para obtener un nuevo par de tokens (válido 7 días)
public record TokenResponseDTO(String accessToken, String refreshToken) {}