package gal.usc.etse.es.tapeacompostela.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.es.tapeacompostela.model.dto.RedemptionDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.RedemptionQrDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.RewardDTO;
import gal.usc.etse.es.tapeacompostela.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Rewards", description = "Rewards of the businesses and their redemptions with points")
@RestController
@RequiredArgsConstructor
public class RewardController {
    private final RewardService rewardService;

    // Las recompensas activas de un negocio
    @Operation(summary = "The active rewards of a business")
    @GetMapping("/rewards")
    public List<RewardDTO> getRewards(@RequestParam Long business) {
        return rewardService.getRewards(business);
    }

    @Operation(summary = "One reward")
    @GetMapping("/rewards/{id}")
    public RewardDTO getReward(@PathVariable Long id) {
        return rewardService.getReward(id);
    }

    // El negocio va en el cuerpo: {"businessId": 1, "title": "...", "pointsCost": 30}
    @Operation(summary = "Add a reward to a business (its owner)")
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
    // El usuario es el del token, nunca el del cuerpo.
    // Cabecera Idempotency-Key (opcional): un valor que elige el cliente, por ejemplo un UUID, distinto para cada canje
    // nuevo y el mismo cuando repite uno. Repetir el POST con la misma clave devuelve el mismo canje
    @Operation(summary = "Spend points on a reward: gives a code to show at the business")
    @PostMapping("/redemptions")
    public ResponseEntity<RedemptionDTO> redeem(@RequestBody @Valid RedemptionDTO redemption, Authentication authentication,
                                                @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        RedemptionDTO created = rewardService.redeem(redemption.rewardId(), authentication.getName(), idempotencyKey);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Los canjes del usuario que ha iniciado sesión, el más reciente primero. Nadie ve los canjes de los demás
    @Operation(summary = "The redemptions of the logged user")
    @GetMapping("/redemptions")
    public List<RedemptionDTO> getMyRedemptions(Authentication authentication) {
        return rewardService.getRedemptions(authentication.getName());
    }

    @Operation(summary = "One redemption (who redeemed it or an admin)")
    @GetMapping("/redemptions/{id}")
    public RedemptionDTO getRedemption(@PathVariable Long id) {
        return rewardService.getRedemption(id);
    }

    // El QR del canje: un subrecurso suyo, como /businesses/{id}/summary del negocio. Solo lo ve quien lo canjeó
    @Operation(summary = "The QR of a pending redemption; it changes every 30 seconds")
    @GetMapping("/redemptions/{id}/qr")
    public RedemptionQrDTO getQr(@PathVariable Long id, Authentication authentication) {
        return rewardService.getQr(id, authentication.getName());
    }

    // La cabecera Redemption-QR: el QR que el negocio acaba de escanear del teléfono del cliente
    @Operation(summary = "Mark a redemption as USED with JSON Patch (the owner of the business, with the scanned QR)")
    @PatchMapping(path = "/redemptions/{id}", consumes = "application/json-patch+json")
    public RedemptionDTO updateRedemption(@PathVariable Long id, @RequestBody List<Map<String, Object>> updates,
                                          @RequestHeader("Redemption-QR") String qr,
                                          Authentication authentication) throws JsonPatchException {
        return rewardService.updateRedemption(id, updates, authentication.getName(), qr);
    }
} 
