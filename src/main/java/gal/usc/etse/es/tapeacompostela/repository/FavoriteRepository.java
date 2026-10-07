package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Page<Favorite> findAllByUserId(Long userId, Pageable page);
    Optional<Favorite> findByUserIdAndBusinessId(Long userId, Long businessId);
    void deleteAllByUserId(Long userId);
    void deleteAllByBusinessId(Long businessId);
}
