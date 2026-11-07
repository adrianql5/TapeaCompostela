package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    // Los locales que tienen tag, busca por Business.tags y los compada con Tag.id
    List<Business> findAllByTagsId(Long tagId);
}