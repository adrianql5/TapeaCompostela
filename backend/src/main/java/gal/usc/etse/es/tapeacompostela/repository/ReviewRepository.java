package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Review;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Paginación por cursor (reemplaza a la consulta con Page): la primera página son las reseñas más nuevas
    // de un negocio, y las siguientes las que tienen un id menor que el cursor. Con su autor en la misma consulta
    @EntityGraph(attributePaths = {"user"})
    List<Review> findByBusinessIdOrderByIdDesc(Long businessId, Limit limit);

    @EntityGraph(attributePaths = {"user"})
    List<Review> findByBusinessIdAndIdLessThanOrderByIdDesc(Long businessId, Long cursor, Limit limit);

    boolean existsByUserUsernameAndBusinessId(String username, Long businessId);

    long countByBusinessId(Long businessId);

    // JPQL: como SQL, pero con las entidades y sus atributos (Review, r.business.id) en vez de tablas y columnas.
    // AVG devuelve null cuando el negocio no tiene reseñas
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.business.id = :businessId")
    Double averageRating(@Param("businessId") Long businessId);
}