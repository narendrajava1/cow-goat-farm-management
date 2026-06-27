package com.cgfms.animal.repository;

import com.cgfms.animal.domain.Herd;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface HerdRepository extends R2dbcRepository<Herd, UUID> {

    @Query("SELECT COUNT(*) > 0 FROM herds WHERE farm_id = :farmId AND name = :name")
    Mono<Boolean> existsByFarmIdAndName(UUID farmId, String name);

    Flux<Herd> findByFarmId(UUID farmId);
}
