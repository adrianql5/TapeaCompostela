package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}