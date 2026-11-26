package gal.usc.etse.es.tapeacompostela.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.es.tapeacompostela.exception.ConflictException;
import gal.usc.etse.es.tapeacompostela.exception.NotEnoughPointsException;
import gal.usc.etse.es.tapeacompostela.exception.NotFoundException;
import gal.usc.etse.es.tapeacompostela.model.dto.RedemptionDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.RedemptionQrDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.RewardDTO;
import gal.usc.etse.es.tapeacompostela.model.entity.Business;
import gal.usc.etse.es.tapeacompostela.model.entity.Redemption;
import gal.usc.etse.es.tapeacompostela.model.entity.Redemption.Status;
import gal.usc.etse.es.tapeacompostela.model.entity.Reward;
import gal.usc.etse.es.tapeacompostela.model.entity.User;
import gal.usc.etse.es.tapeacompostela.repository.BusinessRepository;
import gal.usc.etse.es.tapeacompostela.repository.RedemptionRepository;
import gal.usc.etse.es.tapeacompostela.repository.RewardRepository;
import gal.usc.etse.es.tapeacompostela.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardService {
    private final RewardRepository rewards;
    private final RedemptionRepository redemptions;
    private final BusinessRepository businesses;
    private final UserRepository users;
    private final PatchUtils patchUtils;
    private final QrService qrService;

    // Las recompensas activas de un negocio
    public List<RewardDTO> getRewards(Long businessId) {
        if (!businesses.existsById(businessId)) {
            throw new NotFoundException("Business", businessId);
        }
        return rewards.findAllByBusinessIdAndActiveTrue(businessId).stream().map(this::toDTO).toList();
    }

    public RewardDTO getReward(Long id) {
        return rewards.findById(id).map(this::toDTO).orElseThrow(() -> new NotFoundException("Reward", id));
    }

    // Solo el dueño del negocio le añade recompensas
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public RewardDTO createReward(RewardDTO reward, String username) {
        Long businessId = reward.businessId();
        Business business = businesses.findById(businessId).orElseThrow(() -> new NotFoundException("Business", businessId));
        checkOwner(business, username);
        Reward saved = rewards.save(Reward.builder()
                .business(business)
                .title(reward.title())
                .description(reward.description())
                .pointsCost(reward.pointsCost())
                .active(!Boolean.FALSE.equals(reward.active()))
                .build());
        return toDTO(saved);
    }

    // Dos cambios que van juntos: los puntos del usuario bajan y se crea el canje.
    // Los dos en una transacción: si uno falla, no se guarda ninguno
    // idempotencyKey (opcional): si ya hay un canje creado con esta clave, se devuelve tal cual,
    // sin gastar puntos otra vez. Así la app puede repetir el POST cuando falla la red sin pagar dos veces
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public RedemptionDTO redeem(Long rewardId, String username, String idempotencyKey) {
        if (idempotencyKey != null) {
            var previous = redemptions.findByIdempotencyKey(idempotencyKey);
            if (previous.isPresent()) {
                // La misma clave de otro usuario, o para otra recompensa, es una petición distinta: se rechaza
                if (!previous.get().getUser().getUsername().equals(username) || !previous.get().getReward().getId().equals(rewardId)) {
                    throw new ConflictException("The Idempotency-Key " + idempotencyKey + " was used for another request");
                }
                return toDTO(previous.get());
            }
        }

        Reward reward = rewards.findById(rewardId).orElseThrow(() -> new NotFoundException("Reward", rewardId));
        if (!reward.isActive()) {
            throw new ConflictException("Reward " + rewardId + " is not active");
        }
        User user = users.findByUsername(username).orElseThrow(() -> new NotFoundException("User", username));
        if (user.getPoints() < reward.getPointsCost()) {
            throw new NotEnoughPointsException(reward.getPointsCost(), user.getPoints());
        }

        user.setPoints(user.getPoints() - reward.getPointsCost());
        Redemption saved = redemptions.save(Redemption.builder()
                .user(user)
                .reward(reward)
                // Por ejemplo TPC-3F9A1C2B: 8 caracteres aleatorios de un UUID
                .code("TPC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .idempotencyKey(idempotencyKey)
                .build());
        return toDTO(saved);
    }

    // Solo lo ve el usuario que lo canjeó (o un admin)
    @PostAuthorize("returnObject.user() == authentication.name or hasRole('ADMIN')")
    public RedemptionDTO getRedemption(Long id) {
        return redemptions.findById(id).map(this::toDTO).orElseThrow(() -> new NotFoundException("Redemption", id));
    }

    public List<RedemptionDTO> getRedemptions(String username) {
        return redemptions.findAllByUserUsername(username, Sort.by(Sort.Direction.DESC, "redeemedAt")).stream().map(this::toDTO).toList();
    }

    // El QR de un canje pendiente, solo para el usuario que lo canjeó. Cambia cada 30 segundos: cuando llega
    // expiresAt, la app pide uno nuevo
    @PreAuthorize("hasRole('USER')")
    public RedemptionQrDTO getQr(Long id, String username) {
        Redemption redemption = redemptions.findById(id).orElseThrow(() -> new NotFoundException("Redemption", id));
        if (!redemption.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Redemption " + id + " is not yours");
        }
        if (redemption.getStatus() != Status.PENDING) {
            throw new ConflictException("Redemption " + redemption.getCode() + " is " + redemption.getStatus());
        }
        return new RedemptionQrDTO(qrService.create(id), qrService.nextChange());
    }

    // El propietario del negocio marca el código como usado cuando el cliente lo enseña: [{"op": "replace", "path": "/status", "value": "USED"}].
    // Solo puede cambiar el estado, solo desde PENDING y solo a USED. qr es el QR que el negocio acaba de escanear
    // del teléfono del cliente: sin uno válido y reciente, el canje no se marca como usado
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public RedemptionDTO updateRedemption(Long id, List<Map<String, Object>> updates, String username, String qr) throws JsonPatchException {
        Redemption redemption = redemptions.findById(id).orElseThrow(() -> new NotFoundException("Redemption", id));
        checkOwner(redemption.getReward().getBusiness(), username);
        if (redemption.getStatus() != Status.PENDING) {
            throw new ConflictException("Redemption " + redemption.getCode() + " is " + redemption.getStatus());
        }
        if (!qrService.isValid(qr, id)) {
            // ErrorResponseException, como NotEnoughPointsException, pero sin una clase propia: un 400 con este texto
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The QR is not valid or has expired"), null);
        }

        RedemptionDTO patched = patchUtils.applyPatch(toDTO(redemption), updates, Set.of("status"));
        if (patched.status() != Status.USED) {
            throw new JsonPatchException("A pending redemption can only become USED");
        }
        redemption.setStatus(Status.USED);
        redemption.setUsedAt(LocalDateTime.now());
        return toDTO(redemption);
    }

    // Un propietario solo puede tocar las recompensas y los canjes de sus propios negocios (si no, 403)
    private void checkOwner(Business business, String username) {
        if (business.getOwner() == null || !business.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Business " + business.getId() + " is not yours");
        }
    }

    private RewardDTO toDTO(Reward reward) {
        return new RewardDTO(reward.getId(), reward.getBusiness().getId(), reward.getTitle(), reward.getDescription(),
                reward.getPointsCost(), reward.isActive());
    }

    private RedemptionDTO toDTO(Redemption redemption) {
        return new RedemptionDTO(
                redemption.getId(),
                redemption.getReward().getId(),
                redemption.getReward().getTitle(),
                redemption.getReward().getBusiness().getId(),
                redemption.getUser().getUsername(),
                redemption.getCode(),
                redemption.getStatus(),
                redemption.getRedeemedAt(),
                redemption.getUsedAt()
        );
    }
}