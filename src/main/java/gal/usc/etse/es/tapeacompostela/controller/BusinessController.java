package gal.usc.etse.es.tapeacompostela.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.es.tapeacompostela.model.dto.BusinessDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.BusinessSummaryDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.ReviewDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.RewardDTO;
import gal.usc.etse.es.tapeacompostela.model.entity.Business.PriceRange;
import gal.usc.etse.es.tapeacompostela.service.BusinessService;
import gal.usc.etse.es.tapeacompostela.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Tag(name = "Businesses", description = "Search, read and manage the businesses")
@RestController
@RequestMapping("/businesses")
@RequiredArgsConstructor
public class BusinessController {
    private final BusinessService businessService;
    private final SummaryService summaryService;

    // GET /businesses?name=taberna&priceRange=MODERATE&page=0&size=5&sort=averageRating&direction=DESC
    // Cada parámetro es opcional. La respuesta es una página de negocios, con los campos de la vista de resumen
    @Operation(summary = "Search businesses by name and price range, one page at a time")
    @GetMapping
    @JsonView(BusinessDTO.Views.Summary.class)
    public Page<BusinessDTO> getBusinesses(
         @RequestParam(required = false) String name,
         @RequestParam(required = false) PriceRange priceRange,
         @RequestParam(defaultValue = "0") @Min(0) int page,
         @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
         @RequestParam(defaultValue = "name") String sort,
         @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ){
        return businessService.getBusinesses(name, priceRange, PageRequest.of(page, size, Sort.by(direction, sort)));
    }

    // Un negocio con todos sus campos (vista Detail). Versión 1.0
    @Operation(summary = "One business, with all its fields")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The business",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusinessDTO.class))),
            @ApiResponse(responseCode = "404", description = "There is no business with that id",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(path = "/{id}", version = "1.0")
    @JsonView(BusinessDTO.Views.Detail.class)
    public BusinessDTO getBusiness(@PathVariable Long id) {
        return businessService.getBusiness(id);
    }

    // Versión 2.0: las coordenadas van juntas en "location". Un cliente de la versión 1.0
    // lee "latitude" y "longitude", así que cambiar la 1.0 lo rompería: por eso el cambio
    // va en una versión nueva y la 1.0 sigue igual
    public record LocationDTO(Double latitude, Double longitude) {}

    public record BusinessV2DTO(Long id, String name, String address, LocationDTO location, String owner) {}

    @Operation(summary = "One business, with its coordinates together in location (API-Version: 2.0)")
    @GetMapping(path = "/{id}", version = "2.0")
    public BusinessV2DTO getBusinessV2(@PathVariable Long id) {
        BusinessDTO business = businessService.getBusiness(id);
        return new BusinessV2DTO(business.id(), business.name(), business.address(),
                new LocationDTO(business.latitude(), business.longitude()), business.owner());
    }

    // 201 Created con la cabecera Location apuntando al nuevo negocio
    @Operation(summary = "Create a business (owners and admins). The logged user is its owner")
    @PostMapping
    public ResponseEntity<BusinessDTO> createBusiness(@RequestBody @Valid BusinessDTO business, Authentication authentication) {
        BusinessDTO created = businessService.createBusiness(business, authentication.getName());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Replace a whole business (its owner or an admin)")
    @PutMapping("/{id}")
    public BusinessDTO replaceBusiness(@PathVariable Long id, @RequestBody @Valid BusinessDTO business) {
        return businessService.replaceBusiness(id, business);
    }

    // Las tres lecturas empiezan en el mismo instante, y la respuesta espera por todas ellas
    // le lleva tanto como le lleve a la mas lenta
    @Operation(summary = "A business with its 3 newest reviews and its rewards, read in parallel")
    @GetMapping("/{id}/summary")
    public BusinessSummaryDTO getSummary(@PathVariable Long id) throws InterruptedException, ExecutionException {
        CompletableFuture<BusinessDTO> business = summaryService.getBusiness(id);
        CompletableFuture<List<ReviewDTO>> reviews = summaryService.getLatestReviews(id);
        CompletableFuture<List<RewardDTO>> rewards = summaryService.getRewards(id);

        CompletableFuture.allOf(business, reviews, rewards).join();
        return new BusinessSummaryDTO(business.get(), reviews.get(), rewards.get());
    }

    // El cuerpo es una lista de operaciones [{"op": "replace", "path": "/phone", "value": "981000000"}] 
    @Operation(summary = "Change some fields of a business with JSON Patch (its owner or an admin)")
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    @JsonView(BusinessDTO.Views.Detail.class)
    public BusinessDTO updateBusiness(@PathVariable Long id, @RequestBody List<Map<String, Object>> updates) throws JsonPatchException {
        return businessService.updateBusiness(id, updates);
    }

    @Operation(summary = "Delete a business with its reviews and rewards (its owner or an admin)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);
    }
}