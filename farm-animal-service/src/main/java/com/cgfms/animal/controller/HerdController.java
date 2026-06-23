package com.cgfms.animal.controller;

import com.cgfms.animal.dto.request.HerdCreateRequest;
import com.cgfms.animal.dto.response.AnimalResponse;
import com.cgfms.animal.dto.response.HerdResponse;
import com.cgfms.animal.service.HerdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/herds")
@RequiredArgsConstructor
public class HerdController {
    
    private final HerdService herdService;
    
    @PostMapping
    public ResponseEntity<HerdResponse> createHerd(
            @Valid @RequestBody HerdCreateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        HerdResponse response = herdService.createHerd(request, farmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{herdId}")
    public ResponseEntity<HerdResponse> getHerd(
            @PathVariable UUID herdId,
            @RequestHeader("farm-id") UUID farmId) {
        HerdResponse response = herdService.getHerdById(herdId, farmId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{herdId}")
    public ResponseEntity<HerdResponse> updateHerd(
            @PathVariable UUID herdId,
            @Valid @RequestBody HerdCreateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        HerdResponse response = herdService.updateHerd(herdId, request, farmId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{herdId}")
    public ResponseEntity<Void> deleteHerd(
            @PathVariable UUID herdId,
            @RequestHeader("farm-id") UUID farmId) {
        herdService.deleteHerd(herdId, farmId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{herdId}/animals")
    public ResponseEntity<List<AnimalResponse>> getAnimalsInHerd(
            @PathVariable UUID herdId,
            @RequestHeader("farm-id") UUID farmId) {
        List<AnimalResponse> response = herdService.getAnimalsByHerdId(herdId, farmId).stream()
                .map(animal -> new AnimalResponse()) // Simplified for now - would use a proper mapper
                .toList();
        return ResponseEntity.ok(response);
    }
}