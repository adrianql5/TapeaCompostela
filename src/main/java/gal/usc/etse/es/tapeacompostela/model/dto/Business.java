package gal.usc.etse.es.tapeacompostela.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import gal.usc.etse.es.tapeacompostela.model.entity.Business.PriceRange;
import gal.usc.etse.es.tapeacompostela.model.entity.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Relation(collectionRelation = "businesses")
public record Business (
    @JsonView(Views.Summary.class) Long id,
    @JsonView(Views.Summary.class) @NotBlank String name,
    @JsonView(Views.Summary.class) @NotBlank String address,
    @JsonView(Views.Summary.class) @NotNull Double latitude,
    @JsonView(Views.Summary.class) @NotNull Double longitude,
    @JsonView(Views.Summary.class) PriceRange priceRange,
    @JsonView(Views.Summary.class) BigDecimal averageRating,
    @JsonView(Views.Summary.class) Integer ratingCount,
    @JsonView(Views.Summary.class) String imageUrl,
    @JsonView(Views.Summary.class) Set<String> tags,
    @JsonView(Views.Complete.class) String description,
    @JsonView(Views.Summary.class) Boolean partner,
    @JsonView(Views.Summary.class) Long ownerId,
    @JsonView(Views.Complete.class) String phone,
    @JsonView(Views.Complete.class) String website,
    @JsonView(Views.Complete.class) LocalDateTime createdAt
) {
    public interface Views {
        interface Summary {}
        interface Complete extends Summary {}
    }

    public static Business from(gal.usc.etse.es.tapeacompostela.model.entity.Business business) {
        return new Business(
                business.getId(),
                business.getName(),
                business.getAddress(),
                business.getLatitude(),
                business.getLongitude(),
                business.getPriceRange(),
                business.getAverageRating(),
                business.getRatingCount(),
                business.getImageUrl(),
                business.getTags().stream().map(Tag::getName).collect(Collectors.toSet()),
                business.getDescription(),
                business.isPartner(),
                business.getOwner() == null ? null : business.getOwner().getId(),
                business.getPhone(),
                business.getWebsite(),
                business.getCreatedAt()
        );
    }
}
