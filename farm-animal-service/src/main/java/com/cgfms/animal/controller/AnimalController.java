package com.cgfms.animal.controller;

import com.cgfms.animal.dto.request.AnimalCreateRequest;
import com.cgfms.animal.dto.request.AnimalStatusUpdateRequest;
import com.cgfms.animal.dto.request.AnimalUpdateRequest;
import com.cgfms.animal.dto.response.AnimalResponse;
import com.cgfms.animal.service.AnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/animals")
@RequiredArgsConstructor
public class AnimalController {
    
    private final AnimalService animalService;
    
    @PostMapping
    public ResponseEntity<AnimalResponse> registerAnimal(
            @Valid @RequestBody AnimalCreateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        AnimalResponse response = animalService.registerAnimal(request, farmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{animalId}")
    public ResponseEntity<AnimalResponse> getAnimal(
            @PathVariable UUID animalId,
            @RequestHeader("farm-id") UUID farmId) {
        AnimalResponse response = animalService.getAnimalById(animalId, farmId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<AnimalResponse>> listAnimals(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String status,
            @RequestHeader("farm-id") UUID farmId) {
        List<AnimalResponse> response = animalService.listAnimals(farmId, type, gender, status);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{animalId}")
    public ResponseEntity<AnimalResponse> updateAnimal(
            @PathVariable UUID animalId,
            @Valid @RequestBody AnimalUpdateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        AnimalResponse response = animalService.updateAnimal(animalId, request, farmId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{animalId}/status")
    public ResponseEntity<AnimalResponse> changeStatus(
            @PathVariable UUID animalId,
            @Valid @RequestBody AnimalStatusUpdateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        AnimalResponse response = animalService.changeAnimalStatus(animalId, request.getStatus(), farmId);
        return ResponseEntity.ok(response);
    }
}