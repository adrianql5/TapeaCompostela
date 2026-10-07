package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Redemption.Status;
import gal.usc.etse.es.tapeacompostela.model.entity.Redemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    Page<Redemption> findAllByUserId(Long userId, Pageable page);
    Page<Redemption> findAllByRewardBusinessId(Long businessId, Pageable page);
    Page<Redemption> findAllByRewardBusinessIdAndCode(Long businessId, String code, Pageable page);
    boolean existsByCode(String code);
    List<Redemption> findAllByStatusAndRedeemedAtBefore(Status status, LocalDateTime moment);
    void deleteAllByRewardBusinessId(Long businessId);
}
