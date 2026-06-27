package com.cgfms.animal.service;

import com.cgfms.animal.cache.HerdCacheService;
import com.cgfms.animal.domain.Animal;
import com.cgfms.animal.domain.Herd;
import com.cgfms.animal.dto.request.HerdCreateRequest;
import com.cgfms.animal.dto.response.HerdResponse;
import com.cgfms.animal.exception.BusinessException;
import com.cgfms.animal.exception.ResourceNotFoundException;
import com.cgfms.animal.mapper.HerdMapper;
import com.cgfms.animal.repository.AnimalRepository;
import com.cgfms.animal.repository.HerdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HerdService {

    private final HerdRepository herdRepository;
    private final AnimalRepository animalRepository;
    private final HerdMapper herdMapper;
    private final HerdCacheService herdCacheService;

    public Mono<HerdResponse> createHerd(HerdCreateRequest request, UUID farmId) {
        return herdRepository.existsByFarmIdAndName(farmId, request.getName())
                .flatMap(exists -> {
                    if (exists) return Mono.error(new BusinessException(
                            "Herd '" + request.getName() + "' already exists in this farm"));
                    Herd herd = herdMapper.toEntity(request);
                    herd.setFarmId(farmId);
                    return herdRepository.save(herd)
                            .map(herdMapper::toResponse)
                            .flatMap(response -> herdCacheService.put(response)
                                    .then(herdCacheService.evictList(farmId))
                                    .thenReturn(response))
                            .doOnSuccess(r -> log.info("Herd created: name={}, farmId={}", r.getName(), farmId));
                });
    }

    public Mono<HerdResponse> getHerdById(UUID herdId, UUID farmId) {
        return herdCacheService.get(herdId)
                .switchIfEmpty(
                        herdRepository.findById(herdId)
                                .filter(h -> h.getFarmId().equals(farmId))
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Herd", herdId)))
                                .map(herdMapper::toResponse)
                                .flatMap(herdCacheService::put)
                );
    }

    public Mono<HerdResponse> updateHerd(UUID herdId, HerdCreateRequest request, UUID farmId) {
        return herdRepository.findById(herdId)
                .filter(h -> h.getFarmId().equals(farmId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Herd", herdId)))
                .flatMap(herd -> {
                    herd.setName(request.getName());
                    herd.setDescription(request.getDescription());
                    herd.setHerdType(request.getHerdType());
                    return herdRepository.save(herd);
                })
                .map(herdMapper::toResponse)
                .flatMap(response -> herdCacheService.evictAll(herdId, farmId)
                        .thenReturn(response))
                .doOnSuccess(r -> log.info("Herd updated: id={}", herdId));
    }

    public Mono<Void> deleteHerd(UUID herdId, UUID farmId) {
        return herdRepository.findById(herdId)
                .filter(h -> h.getFarmId().equals(farmId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Herd", herdId)))
                .flatMap(herd -> herdRepository.delete(herd)
                        .then(herdCacheService.evictAll(herdId, farmId)))
                .doOnSuccess(v -> log.info("Herd deleted: id={}", herdId));
    }

    // ← Missing method that HerdController calls
    public Flux<Animal> getAnimalsByHerdId(UUID herdId, UUID farmId) {
        return herdRepository.findById(herdId)
                .filter(h -> h.getFarmId().equals(farmId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Herd", herdId)))
                .flatMapMany(herd -> animalRepository.findByHerdId(herdId));
    }
}