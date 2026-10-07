package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    Page<Reward> findAllByBusinessId(Long businessId, Pageable page);
    Optional<Reward> findByBusinessIdAndTitle(Long businessId, String title);
    void deleteAllByBusinessId(Long businessId);
}
