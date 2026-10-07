package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.PointTransaction.Type;
import gal.usc.etse.es.tapeacompostela.model.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    Page<PointTransaction> findAllByUserId(Long userId, Pageable page);
    Page<PointTransaction> findAllByUserIdAndBusinessId(Long userId, Long businessId, Pageable page);
    Page<PointTransaction> findAllByBusinessId(Long businessId, Pageable page);
    List<PointTransaction> findAllByUserId(Long userId);
    List<PointTransaction> findAllByUserIdAndBusinessId(Long userId, Long businessId);
    List<PointTransaction> findAllByUserIdAndType(Long userId, Type type);
    void deleteAllByBusinessId(Long businessId);
}
