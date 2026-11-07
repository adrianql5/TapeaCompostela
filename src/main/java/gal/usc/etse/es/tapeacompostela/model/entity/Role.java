package gal.usc.etse.es.tapeacompostela.model.entity;

import jakarta.persistence.*;
import lombok.*;

// USER, OWNER o ADMIN. El nombre es la clave primaria, hay 3 y nunca cambian
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @Column(length = 20)
    private String name;
}
