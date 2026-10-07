package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Tag(
        Long id,
        @NotBlank @Size(max = 50) String name
) {
    public static Tag from(gal.usc.etse.es.tapeacompostela.model.entity.Tag tag) {
        return new Tag(tag.getId(), tag.getName());
    }
}
