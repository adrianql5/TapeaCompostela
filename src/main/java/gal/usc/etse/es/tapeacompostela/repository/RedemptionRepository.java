package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Redemption;
import gal.usc.etse.es.tapeacompostela.model.entity.Redemption.Status;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    @EntityGraph(attributePaths = {"user", "reward", "reward.business"})
    List<Redemption> findAllByUserUsername(String username, Sort sort);

    @Override
    @EntityGraph(attributePaths = {"user", "reward", "reward.business", "reward.business.owner"})
    Optional<Redemption> findById(Long id);

    // Los canjes en un estado y anteriores a un momento: con PENDING y la fecha límite, los que han caducado.
    // Lo usa RedemptionExpiryJob
    List<Redemption> findAllByStatusAndRedeemedAtBefore(Status status, LocalDateTime moment);
}