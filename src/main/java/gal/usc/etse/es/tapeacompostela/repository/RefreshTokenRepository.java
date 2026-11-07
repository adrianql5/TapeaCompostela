package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}