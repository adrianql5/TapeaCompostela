package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.OpeningHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpeningHourRepository extends JpaRepository<OpeningHour, Long> {
    List<OpeningHour> findAllByBusinessIdOrderByDayOfWeekAscOpensAtAsc(Long businessId);
    void deleteAllByBusinessId(Long businessId);
}
