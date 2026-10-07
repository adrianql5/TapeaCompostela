package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.MeetupParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetupParticipantRepository extends JpaRepository<MeetupParticipant, Long> {
    List<MeetupParticipant> findAllByMeetupId(Long meetupId);
    Optional<MeetupParticipant> findByMeetupIdAndUserId(Long meetupId, Long userId);
    boolean existsByMeetupIdAndUserUsername(Long meetupId, String username);
    void deleteAllByMeetupId(Long meetupId);
    void deleteAllByUserId(Long userId);
}
