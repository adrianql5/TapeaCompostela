package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}