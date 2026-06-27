package com.cgfms.animal.cache;

import com.cgfms.animal.dto.response.HerdResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class HerdCacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private static final Duration TTL = Duration.ofMinutes(60); // herds change less often
    private static final String PREFIX = "herd:";
    private static final String FARM_LIST_PREFIX = "herds:farm:";

    // ─── Key helpers ───────────────────────────────────────────────

    private String herdKey(UUID herdId) {
        return PREFIX + herdId;
    }

    private String farmListKey(UUID farmId) {
        return FARM_LIST_PREFIX + farmId;
    }

    // ─── Single herd ───────────────────────────────────────────────

    public Mono<HerdResponse> get(UUID herdId) {
        return redisTemplate.opsForValue()
                .get(herdKey(herdId))
                .cast(HerdResponse.class)
                .doOnNext(h -> log.debug("Cache HIT  herd:{}", herdId))
                .doOnSuccess(h -> {
                    if (h == null) log.debug("Cache MISS herd:{}", herdId);
                });
    }

    public Mono<HerdResponse> put(HerdResponse response) {
        return redisTemplate.opsForValue()
                .set(herdKey(response.getId()), response, TTL)
                .thenReturn(response)
                .doOnSuccess(h -> log.debug("Cache PUT  herd:{}", response.getId()));
    }

    public Mono<Void> evict(UUID herdId) {
        return redisTemplate.opsForValue()
                .delete(herdKey(herdId))
                .doOnSuccess(d -> log.debug("Cache EVICT herd:{}", herdId))
                .then();
    }

    // ─── Farm herd list ────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Mono<List<HerdResponse>> getList(UUID farmId) {
        return redisTemplate.opsForValue()
                .get(farmListKey(farmId))
                .cast(List.class)
                .map(list -> (List<HerdResponse>) list)
                .doOnNext(l -> log.debug("Cache HIT  herds:farm:{}", farmId))
                .doOnSuccess(l -> {
                    if (l == null) log.debug("Cache MISS herds:farm:{}", farmId);
                });
    }

    public Mono<List<HerdResponse>> putList(UUID farmId, List<HerdResponse> herds) {
        return redisTemplate.opsForValue()
                .set(farmListKey(farmId), herds, TTL)
                .thenReturn(herds)
                .doOnSuccess(l -> log.debug("Cache PUT  herds:farm:{} ({} items)", farmId, l.size()));
    }

    public Mono<Void> evictList(UUID farmId) {
        return redisTemplate.opsForValue()
                .delete(farmListKey(farmId))
                .doOnSuccess(d -> log.debug("Cache EVICT herds:farm:{}", farmId))
                .then();
    }

    // ─── Evict all caches for a herd (single + farm list) ─────────

    public Mono<Void> evictAll(UUID herdId, UUID farmId) {
        return Mono.when(evict(herdId), evictList(farmId));
    }
}