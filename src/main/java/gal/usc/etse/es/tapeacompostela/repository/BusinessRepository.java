package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// The Specification executor builds the search of businesses, whose filters QBE cannot express (tags, hours, distance)
public interface BusinessRepository extends JpaRepository<Business, Long>, JpaSpecificationExecutor<Business> {
    List<Business> findAllByOwnerId(Long ownerId);
    List<Business> findAllByTagsName(String name);
}
