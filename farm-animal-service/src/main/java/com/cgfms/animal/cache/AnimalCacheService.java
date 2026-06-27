package com.cgfms.animal.cache;

import com.cgfms.animal.dto.response.AnimalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnimalCacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private static final Duration TTL = Duration.ofMinutes(30);
    private static final String PREFIX = "animal:";
    private static final String FARM_LIST_PREFIX = "animals:farm:";

    // ─── Key helpers ───────────────────────────────────────────────

    private String animalKey(UUID animalId) {
        return PREFIX + animalId;
    }

    private String farmListKey(UUID farmId) {
        return FARM_LIST_PREFIX + farmId;
    }

    // ─── Single animal ─────────────────────────────────────────────

    public Mono<AnimalResponse> get(UUID animalId) {
        return redisTemplate.opsForValue()
                .get(animalKey(animalId))
                .cast(AnimalResponse.class)
                .doOnNext(a -> log.debug("Cache HIT  animal:{}", animalId))
                .doOnSuccess(a -> {
                    if (a == null) log.debug("Cache MISS animal:{}", animalId);
                });
    }

    public Mono<AnimalResponse> put(AnimalResponse response) {
        return redisTemplate.opsForValue()
                .set(animalKey(response.getId()), response, TTL)
                .thenReturn(response)
                .doOnSuccess(a -> log.debug("Cache PUT  animal:{}", response.getId()));
    }

    public Mono<Void> evict(UUID animalId) {
        return redisTemplate.opsForValue()
                .delete(animalKey(animalId))
                .doOnSuccess(d -> log.debug("Cache EVICT animal:{}", animalId))
                .then();
    }

    // ─── Farm animal list ──────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Mono<List<AnimalResponse>> getList(UUID farmId) {
        return redisTemplate.opsForValue()
                .get(farmListKey(farmId))
                .cast(List.class)
                .map(list -> (List<AnimalResponse>) list)
                .doOnNext(l -> log.debug("Cache HIT  animals:farm:{}", farmId))
                .doOnSuccess(l -> {
                    if (l == null) log.debug("Cache MISS animals:farm:{}", farmId);
                });
    }

    public Mono<List<AnimalResponse>> putList(UUID farmId, List<AnimalResponse> animals) {
        return redisTemplate.opsForValue()
                .set(farmListKey(farmId), animals, TTL)
                .thenReturn(animals)
                .doOnSuccess(l -> log.debug("Cache PUT  animals:farm:{} ({} items)", farmId, l.size()));
    }

    public Mono<Void> evictList(UUID farmId) {
        return redisTemplate.opsForValue()
                .delete(farmListKey(farmId))
                .doOnSuccess(d -> log.debug("Cache EVICT animals:farm:{}", farmId))
                .then();
    }

    // ─── Evict all caches for an animal (single + farm list) ──────

    public Mono<Void> evictAll(UUID animalId, UUID farmId) {
        return Mono.when(evict(animalId), evictList(farmId));
    }
}