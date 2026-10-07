package gal.usc.etse.es.tapeacompostela.model.dto;

public record Allergen(
        Long id,
        String code,
        String name
) {
    public static Allergen from(gal.usc.etse.es.tapeacompostela.model.entity.Allergen allergen) {
        return new Allergen(allergen.getId(), allergen.getCode(), allergen.getName());
    }
}
