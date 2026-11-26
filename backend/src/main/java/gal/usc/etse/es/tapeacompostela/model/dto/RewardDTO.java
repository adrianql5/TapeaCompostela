package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// En una petición solo se leen businessId, title, description, pointsCost y active; id lo rellena el servidor
public record RewardDTO(
        Long id,
        @NotNull Long businessId,
        @NotBlank @Size(max = 150) String title,
        @Size(max = 1000) String description,
        @NotNull @Positive Integer pointsCost,
        Boolean active
) {}