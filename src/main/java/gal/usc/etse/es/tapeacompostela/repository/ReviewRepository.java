package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Las reseñas de un negocio, una página, con su autor en la misma consulta
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findAllByBusinessId(Long businessId, Pageable page);

    boolean existsByUserUsernameAndBusinessId(String username, Long businessId);

    long countByBusinessId(Long businessId);

    // JPQL: como SQL, pero con las entidades y sus atributos (Review, r.business.id) en vez de tablas y columnas.
    // AVG devuelve null cuando el negocio no tiene reseñas
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.business.id = :businessId")
    Double averageRating(@Param("businessId") Long businessId);
}