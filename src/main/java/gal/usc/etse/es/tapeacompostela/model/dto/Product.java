package gal.usc.etse.es.tapeacompostela.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import gal.usc.etse.es.tapeacompostela.model.entity.Allergen;
import gal.usc.etse.es.tapeacompostela.model.entity.Product.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Relation(collectionRelation = "products")
public record Product(
        @JsonView(Views.Summary.class) Long id,
        @JsonView(Views.Summary.class) Long businessId,
        @JsonView(Views.Summary.class) @NotBlank String name,
        @JsonView(Views.Summary.class) @NotNull @PositiveOrZero BigDecimal price,
        @JsonView(Views.Summary.class) @NotNull Category category,
        @JsonView(Views.Summary.class) String imageUrl,
        @JsonView(Views.Summary.class) Boolean available,
        // Allergen codes, e.g. GLUTEN or MOLLUSCS
        @JsonView(Views.Summary.class) Set<String> allergens,
        @JsonView(Views.Complete.class) String description
) {
    public interface Views {
        interface Summary {}
        interface Complete extends Summary {}
    }

    public static Product from(gal.usc.etse.es.tapeacompostela.model.entity.Product product) {
        return new Product(
                product.getId(),
                product.getBusiness().getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl(),
                product.isAvailable(),
                product.getAllergens().stream().map(Allergen::getCode).collect(Collectors.toSet()),
                product.getDescription()
        );
    }
}
