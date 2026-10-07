package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    // The followers of a user
    Page<Follow> findAllByFollowedId(Long followedId, Pageable page);
    // The users that a user follows
    Page<Follow> findAllByFollowerId(Long followerId, Pageable page);
    List<Follow> findAllByFollowerId(Long followerId);
    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);
    void deleteAllByFollowerId(Long followerId);
    void deleteAllByFollowedId(Long followedId);
}
