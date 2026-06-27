package com.cgfms.animal.controller;

import com.cgfms.animal.dto.request.HerdCreateRequest;
import com.cgfms.animal.dto.response.AnimalResponse;
import com.cgfms.animal.dto.response.HerdResponse;
import com.cgfms.animal.mapper.AnimalMapper;
import com.cgfms.animal.service.HerdService;
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
@RequestMapping("/api/v1/herds")
@RequiredArgsConstructor
public class HerdController {

    private final HerdService herdService;
    private final AnimalMapper animalMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<HerdResponse> createHerd(
            @Valid @RequestBody HerdCreateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        return herdService.createHerd(request, farmId);
    }

    @GetMapping("/{herdId}")
    public Mono<HerdResponse> getHerd(
            @PathVariable UUID herdId,
            @RequestHeader("farm-id") UUID farmId) {
        return herdService.getHerdById(herdId, farmId);
    }

    @PutMapping("/{herdId}")
    public Mono<HerdResponse> updateHerd(
            @PathVariable UUID herdId,
            @Valid @RequestBody HerdCreateRequest request,
            @RequestHeader("farm-id") UUID farmId) {
        return herdService.updateHerd(herdId, request, farmId);
    }

    @DeleteMapping("/{herdId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteHerd(
            @PathVariable UUID herdId,
            @RequestHeader("farm-id") UUID farmId) {
        return herdService.deleteHerd(herdId, farmId);
    }

    @GetMapping("/{herdId}/animals")
    public Flux<AnimalResponse> getAnimalsInHerd(
            @PathVariable UUID herdId,
            @RequestHeader("farm-id") UUID farmId) {
        return herdService.getAnimalsByHerdId(herdId, farmId)
                .map(animalMapper::toResponse);
    }
}
