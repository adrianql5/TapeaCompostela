package gal.usc.etse.es.tapeacompostela.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.es.tapeacompostela.model.dto.RedemptionDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.RewardDTO;
import gal.usc.etse.es.tapeacompostela.service.RewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

// Dos colecciones, como /reviews: /rewards y /redemptions. El negocio de una recompensa y la recompensa de un canje
// son un filtro (?business=1) o un campo del cuerpo (businessId, rewardId), nunca parte del path
@RestController
@RequiredArgsConstructor
public class RewardController {
    private final RewardService rewardService;

    // Las recompensas activas de un negocio
    @GetMapping("/rewards")
    public List<RewardDTO> getRewards(@RequestParam Long business) {
        return rewardService.getRewards(business);
    }

    @GetMapping("/rewards/{id}")
    public RewardDTO getReward(@PathVariable Long id) {
        return rewardService.getReward(id);
    }

    // El negocio va en el cuerpo: {"businessId": 1, "title": "...", "pointsCost": 30}
    @PostMapping("/rewards")
    public ResponseEntity<RewardDTO> createReward(@RequestBody @Valid RewardDTO reward, Authentication authentication) {
        RewardDTO created = rewardService.createReward(reward, authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Canjear una recompensa es crear un canje de ella (sin verbos en la URL): {"rewardId": 2}.
    // El usuario es el del token, nunca el del cuerpo
    @PostMapping("/redemptions")
    public ResponseEntity<RedemptionDTO> redeem(@RequestBody @Valid RedemptionDTO redemption, Authentication authentication) {
        RedemptionDTO created = rewardService.redeem(redemption.rewardId(), authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Los canjes del usuario que ha iniciado sesión, el más reciente primero. Nadie ve los canjes de los demás
    @GetMapping("/redemptions")
    public List<RedemptionDTO> getMyRedemptions(Authentication authentication) {
        return rewardService.getRedemptions(authentication.getName());
    }

    @GetMapping("/redemptions/{id}")
    public RedemptionDTO getRedemption(@PathVariable Long id) {
        return rewardService.getRedemption(id);
    }

    @PatchMapping(path = "/redemptions/{id}", consumes = "application/json-patch+json")
    public RedemptionDTO updateRedemption(@PathVariable Long id, @RequestBody List<Map<String, Object>> updates,
                                          Authentication authentication) throws JsonPatchException {
        return rewardService.updateRedemption(id, updates, authentication.getName());
    }
} 
