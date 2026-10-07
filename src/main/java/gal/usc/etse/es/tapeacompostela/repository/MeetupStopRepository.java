package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.MeetupStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetupStopRepository extends JpaRepository<MeetupStop, Long> {
    List<MeetupStop> findAllByMeetupIdOrderByStopOrder(Long meetupId);
    void deleteAllByMeetupId(Long meetupId);
    void deleteAllByBusinessId(Long businessId);
}
