package gal.usc.etse.es.tapeacompostela.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * One of the 14 allergens that EU Regulation 1169/2011 requires to be declared.
 */
@Entity
@Table(name = "allergens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    @NotBlank
    private String code;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String name;
}
