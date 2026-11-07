package gal.usc.etse.es.tapeacompostela.controller;

import gal.usc.etse.es.tapeacompostela.model.dto.BusinessDTO;
import gal.usc.etse.es.tapeacompostela.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/businesses")
@RequiredArgsConstructor
public class BusinessController {
    private final BusinessService businessService;

    @GetMapping
    public List<BusinessDTO> getBusinesses() {
        return businessService.getBusinesses();
    }

    @GetMapping("/{id}")
    public BusinessDTO getBusiness(@PathVariable Long id) {
        return businessService.getBusiness(id);
    }

    // 201 created con la cabecera de localización
    @PostMapping
    public ResponseEntity<BusinessDTO> createBusiness(@RequestBody @Valid BusinessDTO business) {
        BusinessDTO created = businessService.createBusiness(business);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public BusinessDTO replaceBusiness(@PathVariable Long id, @RequestBody @Valid BusinessDTO business) {
        return businessService.replaceBusiness(id, business);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);
    }
}