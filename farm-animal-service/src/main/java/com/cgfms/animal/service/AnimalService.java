package com.cgfms.animal.service;

import com.cgfms.animal.cache.AnimalCacheService;
import com.cgfms.animal.cache.HerdCacheService;
import com.cgfms.animal.domain.Animal;
import com.cgfms.animal.domain.AnimalStatus;
import com.cgfms.animal.dto.request.AnimalCreateRequest;
import com.cgfms.animal.dto.request.AnimalUpdateRequest;
import com.cgfms.animal.dto.response.AnimalResponse;
import com.cgfms.animal.exception.DuplicateTagException;
import com.cgfms.animal.exception.ResourceNotFoundException;
import com.cgfms.animal.mapper.AnimalMapper;
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
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final HerdRepository herdRepository;
    private final AnimalMapper mapper;
    private final HerdMapper  herdMapper;

    // inject HerdCacheService too — needed when resolving herd
    private final AnimalCacheService animalCache;
    private final HerdCacheService herdCache;

    public Mono<AnimalResponse> registerAnimal(AnimalCreateRequest request, UUID farmId) {
        return animalRepository.existsByFarmIdAndTagNumber(farmId, request.getTagNumber())
                .flatMap(exists -> {
                    if (exists) return Mono.error(new DuplicateTagException(request.getTagNumber()));
                    Animal animal = mapper.toEntity(request);
                    animal.setFarmId(farmId);
                    return resolveHerd(animal, request.getHerdId())
                            .flatMap(animalRepository::save)
                            .doOnSuccess(s -> log.info("Animal registered: tag={}", s.getTagNumber()))
                            .map(mapper::toResponse)
                            .flatMap(response -> animalCache.put(response)
                                    .then(animalCache.evictList(farmId))  // invalidate farm list
                                    .thenReturn(response));
                });
    }

    public Mono<AnimalResponse> getAnimalById(UUID animalId, UUID farmId) {
        return animalCache.get(animalId)
                .switchIfEmpty(
                        animalRepository.findById(animalId)
                                .filter(a -> a.getFarmId().equals(farmId))
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Animal", animalId)))
                                .map(mapper::toResponse)
                                .flatMap(animalCache::put)
                );
    }

    public Flux<AnimalResponse> listAnimals(UUID farmId, String animalType, String gender, String status) {
        // Only cache unfiltered farm list — filtered queries go straight to DB
        boolean unfiltered = animalType == null && gender == null && status == null;
        if (unfiltered) {
            return animalCache.getList(farmId)
                    .flatMapMany(Flux::fromIterable)
                    .switchIfEmpty(
                            animalRepository.findByFarmIdAndFilters(farmId, null, null, null)
                                    .map(mapper::toResponse)
                                    .collectList()
                                    .flatMap(list -> animalCache.putList(farmId, list))
                                    .flatMapMany(Flux::fromIterable)
                    );
        }
        return animalRepository.findByFarmIdAndFilters(farmId, animalType, gender, status)
                .map(mapper::toResponse);
    }

    public Mono<AnimalResponse> updateAnimal(UUID animalId, AnimalUpdateRequest request, UUID farmId) {
        return animalRepository.findById(animalId)
                .filter(a -> a.getFarmId().equals(farmId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Animal", animalId)))
                .flatMap(animal -> {
                    if (!animal.getTagNumber().equals(request.getTagNumber())) {
                        return animalRepository.existsByFarmIdAndTagNumber(farmId, request.getTagNumber())
                                .flatMap(exists -> {
                                    if (exists) return Mono.error(new DuplicateTagException(request.getTagNumber()));
                                    applyUpdates(animal, request);
                                    return resolveHerd(animal, request.getHerdId());
                                });
                    }
                    applyUpdates(animal, request);
                    return resolveHerd(animal, request.getHerdId());
                })
                .flatMap(animalRepository::save)
                .map(mapper::toResponse)
                .flatMap(response -> animalCache.evictAll(animalId, farmId)
                        .thenReturn(response));
    }

    public Mono<AnimalResponse> changeAnimalStatus(UUID animalId, AnimalStatus status, UUID farmId) {
        return animalRepository.findById(animalId)
                .filter(a -> a.getFarmId().equals(farmId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Animal", animalId)))
                .map(animal -> { animal.setStatus(status); return animal; })
                .flatMap(animalRepository::save)
                .map(mapper::toResponse)
                .flatMap(response -> animalCache.evictAll(animalId, farmId)
                        .thenReturn(response));
    }

    private Mono<Animal> resolveHerd(Animal animal, UUID herdId) {
        if (herdId != null) {
            // Try herd cache first
            return herdCache.get(herdId)
                    .switchIfEmpty(
                            herdRepository.findById(herdId)
                                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Herd", herdId.toString())))
                                    .map(herd -> { /* cache it */ return herdMapper.toResponse(herd) ;})
                    )
                    .map(herd -> { animal.setHerdId(herd.getId()); return animal; });
        }
        animal.setHerdId(null);
        return Mono.just(animal);
    }
    private void applyUpdates(Animal animal, AnimalUpdateRequest request) {
        mapper.updateEntity(request, animal);  // uses AnimalMapper, not HerdMapper
    }
}