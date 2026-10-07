package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.ReviewLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Page<ReviewLike> findAllByReviewId(Long reviewId, Pageable page);
    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);
    long countByReviewId(Long reviewId);
    void deleteAllByReviewId(Long reviewId);
    void deleteAllByUserId(Long userId);
}
