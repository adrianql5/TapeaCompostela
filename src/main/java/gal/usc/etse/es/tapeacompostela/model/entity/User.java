package gal.usc.etse.es.tapeacompostela.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // El hash BCrypt de la contraseña, nunca la contraseña en plano
    @Column(nullable = false)
    private String password;

    // Puntos de fidelidad: ganados con reseñas, se gastan en recompensas
    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Integer points = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // N:N sin atributos propios, una tabla intermedia, user_roles
    @ManyToMany
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}