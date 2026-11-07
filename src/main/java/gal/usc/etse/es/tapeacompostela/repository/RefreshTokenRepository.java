package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // Un token que existe y que no ha sido revocado, con su usuario y los roles del usuario
    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
}