package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Meetup;
import gal.usc.etse.es.tapeacompostela.model.entity.MeetupParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetupRepository extends JpaRepository<Meetup, Long> {
    // Meetups created by the user or to which the user was invited. Meetup has no list of participants,
    // so this cannot be written as a derived query
    @Query("SELECT m FROM Meetup m WHERE m.creator.id = :userId OR EXISTS (SELECT p FROM MeetupParticipant p WHERE p.meetup = m AND p.user.id = :userId)")
    Page<Meetup> findAllByMember(@Param("userId") Long userId, Pageable page);

    // Meetups still to come where the user has an invitation in that status (e.g. INVITED: not answered yet)
    @Query("SELECT p.meetup FROM MeetupParticipant p WHERE p.user.id = :userId AND p.status = :status AND p.meetup.scheduledAt > :now ORDER BY p.meetup.scheduledAt")
    List<Meetup> findInvitations(@Param("userId") Long userId, @Param("status") MeetupParticipant.Status status, @Param("now") LocalDateTime now);
}
