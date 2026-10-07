package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByBusinessId(Long businessId, Pageable page);
    Page<Review> findAllByBusinessIdAndProductId(Long businessId, Long productId, Pageable page);
    Page<Review> findAllByUserId(Long userId, Pageable page);
    Page<Review> findAllByUserIdIn(Collection<Long> userIds, Pageable page);
    List<Review> findAllByUserId(Long userId);
    List<Review> findAllByBusinessIdAndProductIsNull(Long businessId);
    List<Review> findAllByBusinessId(Long businessId);
    // The review that a user wrote about a business (or one of its products) since a moment, e.g. today
    Optional<Review> findFirstByUserIdAndBusinessIdAndProductIsNullAndCreatedAtGreaterThanEqual(Long userId, Long businessId, LocalDateTime since);
    Optional<Review> findFirstByUserIdAndBusinessIdAndProductIdAndCreatedAtGreaterThanEqual(Long userId, Long businessId, Long productId, LocalDateTime since);
}
