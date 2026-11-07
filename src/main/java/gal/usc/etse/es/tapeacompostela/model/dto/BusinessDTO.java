package gal.usc.etse.es.tapeacompostela.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
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
        @JsonView(Views.Summary.class) Long id,
        @JsonView(Views.Summary.class) @NotBlank @Size(max = 150) String name,
        @JsonView(Views.Detail.class) @Size(max = 2000) String description,
        @JsonView(Views.Summary.class) @NotBlank String address,
        @JsonView(Views.Detail.class) @NotNull Double latitude,
        @JsonView(Views.Detail.class) @NotNull Double longitude,
        @JsonView(Views.Summary.class) PriceRange priceRange,
        @JsonView(Views.Detail.class) @Size(max = 20) String phone,
        @JsonView(Views.Summary.class) BigDecimal averageRating,
        @JsonView(Views.Summary.class) Integer ratingCount,
        // Los nombres de los tags
        @JsonView(Views.Summary.class) Set<String> tags,
        // El nombre de usuario del propietario
        @JsonView(Views.Detail.class) String owner,
        @JsonView(Views.Detail.class) LocalDateTime createdAt)
{
        // Summary: los campos de la lista (GET /business)
        // Detail: todos (GET /business/{id})
        // Detail exteinde a Summary, por lo que lo tiene los campos marcados con Summary
        public interface Views{
                interface Summary{}
                interface Detail extends Summary{}
        }
}
