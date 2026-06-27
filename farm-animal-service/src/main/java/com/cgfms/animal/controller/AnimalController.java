package com.cgfms.animal.controller;

import com.cgfms.animal.dto.request.AnimalCreateRequest;
import com.cgfms.animal.dto.request.AnimalStatusUpdateRequest;
import com.cgfms.animal.dto.request.AnimalUpdateRequest;
import com.cgfms.animal.dto.response.AnimalResponse;
import com.cgfms.animal.service.AnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * WebFlux controller — all endpoints return reactive types (Mono/Flux).
 */
@RestController
@RequestMapping("/api/v1/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AnimalResponse> registerAnimal(
            @Valid @RequestBody AnimalCreateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        return animalService.registerAnimal(request, farmId);
    }

    @GetMapping("/{animalId}")
    public Mono<AnimalResponse> getAnimal(
            @PathVariable UUID animalId,
            @RequestHeader("farm-id") UUID farmId) {
        return animalService.getAnimalById(animalId, farmId);
    }

    @GetMapping
    public Flux<AnimalResponse> listAnimals(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String status,
            @RequestHeader("farm-id") UUID farmId) {
        return animalService.listAnimals(farmId, type, gender, status);
    }

    @PutMapping("/{animalId}")
    public Mono<AnimalResponse> updateAnimal(
            @PathVariable UUID animalId,
            @Valid @RequestBody AnimalUpdateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        return animalService.updateAnimal(animalId, request, farmId);
    }

    @PatchMapping("/{animalId}/status")
    public Mono<AnimalResponse> changeStatus(
            @PathVariable UUID animalId,
            @Valid @RequestBody AnimalStatusUpdateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        return animalService.changeAnimalStatus(animalId, request.getStatus(), farmId);
    }
}