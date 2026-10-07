package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.MeetupComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetupCommentRepository extends JpaRepository<MeetupComment, Long> {
    Page<MeetupComment> findAllByMeetupId(Long meetupId, Pageable page);
    void deleteAllByMeetupId(Long meetupId);
}
