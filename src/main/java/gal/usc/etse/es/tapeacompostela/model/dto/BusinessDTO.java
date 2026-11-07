package gal.usc.etse.es.tapeacompostela.model.dto;

import gal.usc.etse.es.tapeacompostela.model.entity.Business.PriceRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

// Cómo se ve un neogcio en el JSON de la API. id,
// avergaRating, ratingCount, owner y createdAt se rellenan en el servidor,
// si el cliente los manda se ingoran
public record BusinessDTO(
        Long id,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotBlank String address,
        @NotNull Double latitude,
        @NotNull Double longitude,
        PriceRange priceRange,
        @Size(max = 20) String phone,
        BigDecimal averageRating,
        Integer ratingCount,
        // Los nombres de los tags
        Set<String> tags,
        // El nombre de usuario del propietario
        String owner,
        LocalDateTime createdAt
) {}