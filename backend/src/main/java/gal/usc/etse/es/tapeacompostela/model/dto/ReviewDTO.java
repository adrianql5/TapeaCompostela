package gal.usc.etse.es.tapeacompostela.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// En una petición solo se leen businessId, rating y comment; el resto lo rellena el servidor.
// Los campos vacíos no se escriben: una reseña sin comentario no tiene "comment" en el JSON
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewDTO(
        Long id,
        @NotNull Long businessId,
        // El username de quien la escribe
        String user,
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 2000) String comment,
        LocalDateTime createdAt
) {}