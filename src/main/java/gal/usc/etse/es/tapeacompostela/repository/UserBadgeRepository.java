package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findAllByUserIdOrderByEarnedAt(Long userId);
    Optional<UserBadge> findByUserIdAndBadgeId(Long userId, Long badgeId);
    void deleteAllByBadgeId(Long badgeId);
}
