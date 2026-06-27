package com.cgfms.animal.repository;

import com.cgfms.animal.domain.Animal;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AnimalRepository extends R2dbcRepository<Animal, UUID> {

    @Query("SELECT * FROM animals WHERE farm_id = :farmId AND tag_number = :tagNumber")
    Mono<Animal> findByFarmIdAndTagNumber(UUID farmId, String tagNumber);

    @Query("SELECT COUNT(*) > 0 FROM animals WHERE farm_id = :farmId AND tag_number = :tagNumber")
    Mono<Boolean> existsByFarmIdAndTagNumber(UUID farmId, String tagNumber);

    @Query("SELECT * FROM animals WHERE farm_id = :farmId AND herd_id = :herdId")
    Flux<Animal> findByFarmIdAndHerdId(UUID farmId, UUID herdId);

    @Query("SELECT * FROM animals WHERE farm_id = :farmId "
            + "AND (:animalType IS NULL OR animal_type = :animalType) "
            + "AND (:gender IS NULL OR gender = :gender) "
            + "AND (:status IS NULL OR status = :status)")
    Flux<Animal> findByFarmIdAndFilters(UUID farmId, String animalType, String gender, String status);
    // AnimalRepository.java — add this method
    Flux<Animal> findByHerdId(UUID herdId);
}
