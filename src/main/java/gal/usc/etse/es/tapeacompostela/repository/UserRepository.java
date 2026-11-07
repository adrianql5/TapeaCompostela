package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}