package com.chanwoopark.service.unifiedbiztool.api.service;

import com.chanwoopark.service.unifiedbiztool.api.model.entity.ApiCache;
import com.chanwoopark.service.unifiedbiztool.api.model.enums.ApiStatus;
import com.chanwoopark.service.unifiedbiztool.common.model.enums.Platform;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;

import java.time.Duration;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Service
public class ApiCacheService {

    private final RedisTemplate<String, ApiCache> apiCacheRedisTemplate;

    private final Map<String, RedisTemplate<String, ?>> redisTemplates;

    private static final Duration CACHE_TTL = Duration.ofMinutes(1);

    private final RedisTemplate<String, String> lockRedisTemplate;

    private final RedissonClient redissonClient;

    private final ObjectMapper objectMapper;

    public <T> List<T> getOrFetch(
            String identifier,
            Supplier<List<T>> retriever,
            Predicate<T> nameFilter,
            Class<T> itemType
    ) {
        String cacheKey = "apiResult:" + identifier;
        String lockKey = "lock:" + identifier;
        String apiStatusKey = "apiStatus:" + identifier;
        // 1차 캐시 확인
        ApiCache apiCache = getApiCache(apiStatusKey);
        if (isCachedDone(apiCache)) {
            List<T> cachedResult = getFromCache(cacheKey, itemType);
            List<T> filtered = cachedResult.stream().filter(nameFilter).toList();
            if (!filtered.isEmpty()) {
                log.info("[Cache HIT] {}({}) - Returning {} filtered item(s)", itemType.getSimpleName(), identifier, filtered.size());
                return filtered;
            }
        }

        if (acquireLock(lockKey)) {
            try {
                // 2차 캐시 재확인 (락 획득 후 다른 스레드가 처리했을 수도 있음)
                apiCache = getApiCache(apiStatusKey);
                if (isCachedDone(apiCache)) {
                    List<T> cachedResult = getFromCache(cacheKey, itemType);
                    List<T> filtered = cachedResult.stream().filter(nameFilter).toList();
                    if (!filtered.isEmpty()) {
                        log.info("[Cache RE-HIT after LOCK] {}({}) - Returning {} filtered item(s)", itemType.getSimpleName(), identifier, filtered.size());
                        return filtered;
                    }
                }

                ApiCache currentStatus = getApiCache(apiStatusKey);
                if (currentStatus != null && currentStatus.getApiStatus() == ApiStatus.PENDING) {
                    // 이미 다른 스레드가 작업 중
                    log.info("[Already Pending] {}({}) - Another thread is already processing",
                            itemType.getSimpleName(), identifier);
                    return waitForResultWithBackoff(identifier, retriever, nameFilter, itemType);
                }

                setPending(apiStatusKey);
                try {
                    log.info("[Fetching] {}({}) - Invoking retriever", itemType.getSimpleName(), identifier);
                    List<T> result = retriever.get();

                    if (result == null || result.isEmpty()) {
                        log.warn("[Empty Result] {}({}) - Retriever returned empty list",
                                itemType.getSimpleName(), identifier);
                        setCache(cacheKey, Collections.emptyList(), itemType);
                        setDone(apiStatusKey);
                        return Collections.emptyList();
                    }

                    setCache(cacheKey, result, itemType);
                    setDone(apiStatusKey);

                    List<T> filtered = result.stream().filter(nameFilter).toList();
                    log.info("[Fetch & Store] {}({}) - Total: {}, Filtered: {}",
                            itemType.getSimpleName(), identifier, result.size(), filtered.size());
                    return filtered;
                } catch (Exception e) {
                    log.error("[ERROR] {}({}) - Exception during fetch", itemType.getSimpleName(), identifier, e);
                    delete(apiStatusKey);  // 실패 시 상태 정리
                    throw new RuntimeException("처리 중 오류 발생", e);
                }
            } finally {
                try {
                    lockRedisTemplate.delete(lockKey);
                } catch (Exception e) {
                    log.warn("[Lock Release Failed] {}({}) - Failed to release lock: {}",
                            itemType.getSimpleName(), identifier, e.getMessage());
                }
            }
        } else {
            log.info("[WAIT] {}({}) - Lock already held, waiting for result...",
                    itemType.getSimpleName(), identifier);
            return waitForResultWithBackoff(identifier, retriever, nameFilter, itemType);
        }
    }

    public <T> List<T> getOrCreateSync(
            String identifier,
            Platform platform,
            Supplier<List<T>> retriever,
            Predicate<T> nameFilter,
            Supplier<List<T>> creator,
            Class<T> itemType
    ) {
        String cacheKey = "apiResult:" + identifier;
        String lockKey = "lock:" + identifier;
        String apiStatusKey = "apiStatus:" + identifier;
        // 1. 캐시 상태 확인
        ApiCache apiCache = getApiCache(apiStatusKey);
        if (isCachedDone(apiCache)) {
            List<T> cachedResult = getFromCache(cacheKey, itemType);
            List<T> filtered = cachedResult.stream().filter(nameFilter).toList();
            if (!filtered.isEmpty()) {
                log.info("[Cache HIT] {}({}) - returning filtered result ({} items)", itemType.getSimpleName(), identifier, filtered.size());
                return filtered;
            }
        }
        // 2. 락 획득
        if (acquireLock(lockKey)) {
            try {
                // 재확인 (혹시 먼저 다른 스레드에서 작업 완료했을 수 있음)
                apiCache = getApiCache(apiStatusKey);
                if (isCachedDone(apiCache)) {
                    List<T> cachedResult = getFromCache(cacheKey, itemType);
                    List<T> filtered = cachedResult.stream().filter(nameFilter).toList();
                    if (!filtered.isEmpty()) {
                        log.info("[Cache RE-HIT after LOCK] {}({}) - returning filtered result ({} items)", itemType.getSimpleName(), identifier, filtered.size());
                        return filtered;
                    }
                }
                setPending(apiStatusKey);
                log.info("[Retrieving] {}({}) - fetching from retriever", itemType.getSimpleName(), identifier);
                // 명확한 예외 처리 추가
                List<T> result;
                try {
                    result = retriever.get();
                } catch (Exception e) {
                    log.error("[Retriever Error] {}({}) - retriever failed: {}", itemType.getSimpleName(), identifier, e.getMessage());
                    delete(apiStatusKey);  // 상태 정리
                    throw e;
                }

                // 결과 처리 개선
                List<T> filtered = result.stream().filter(nameFilter).toList();
                if (filtered.isEmpty()) {
                    log.warn("[Fallback] {}({}) - retriever returned no match, invoking creator", itemType.getSimpleName(), identifier);
                    try {
                        result = creator.get();
                        filtered = result.stream().filter(nameFilter).toList();
                        // creator 호출 성공 후 즉시 캐시에 저장
                        setCache(cacheKey, result, itemType);
                    } catch (Exception e) {
                        log.error("[Creator Error] {}({}) - creator failed: {}", itemType.getSimpleName(), identifier, e.getMessage());
                        delete(apiStatusKey);  // 상태 정리
                        throw e;
                    }
                } else {
                    // 결과가 있을 경우 캐시 저장
                    setCache(cacheKey, result, itemType);
                }
                setDone(apiStatusKey);
                return filtered;
            } finally {
                lockRedisTemplate.delete(lockKey);
            }
        } else {
            log.info("[WAIT] {}({}) - another thread is processing, waiting for result", itemType.getSimpleName(), identifier);
            return waitForResultWithBackoff(identifier, retriever, nameFilter, itemType);
        }
    }
    private <T> List<T> waitForResultWithBackoff(String identifier, Supplier<List<T>> retriever, Predicate<T> nameFilter, Class<T> itemType) {
        String lockKey = "lock:" + identifier;
        String cacheKey = "apiResult:" + identifier;
        String apiStatusKey = "apiStatus:" + identifier;
        String latchKey = "latch:" + identifier;

        // 먼저 캐시 재확인 (다른 스레드가 처리 완료했을 수 있음)
        for (int attempt = 0; attempt < 3; attempt++) {  // 최대 3번 확인
            // 캐시 확인 로직
            ApiCache apiCache = getApiCache(apiStatusKey);
            if (isCachedDone(apiCache)) {
                List<T> cachedResult = getFromCache(cacheKey, itemType);
                if (cachedResult != null) {
                    List<T> filtered = cachedResult.stream().filter(nameFilter).toList();
                    if (!filtered.isEmpty()) {
                        log.info("[Cache Hit during wait] {}({}) - Got result while waiting", itemType.getSimpleName(), identifier);
                        return filtered;
                    }
                }
            }

            // 짧은 대기 후 재확인
            if (attempt < 2) {
                try {
                    Thread.sleep(100 * (1 << attempt));  // 100ms, 200ms 백오프
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // 래치 관리 개선
        RCountDownLatch latch = redissonClient.getCountDownLatch(latchKey);
        boolean latchInitialized = false;

        try {
            // 래치가 이미 존재하는지 확인 (대기할 래치가 있는지)
            if (latch.getCount() > 0) {
                log.info("[Latch Wait] {}({}) - Waiting on existing latch", itemType.getSimpleName(), identifier);
                boolean completed = latch.await(15, TimeUnit.SECONDS);  // 타임아웃 15초로 감소

                // 대기 후 결과 확인
                if (completed) {
                    ApiCache apiCache = getApiCache(apiStatusKey);
                    if (isCachedDone(apiCache)) {
                        List<T> cachedResult = getFromCache(cacheKey, itemType);
                        if (cachedResult != null) {
                            List<T> filtered = cachedResult.stream().filter(nameFilter).toList();
                            if (!filtered.isEmpty()) {
                                return filtered;
                            }
                        }
                    }
                }
            } else {
                // 래치가 없으면 새로 생성 시도
                latchInitialized = latch.trySetCount(1);
                if (latchInitialized) {
                    log.info("[Latch Created] {}({}) - Created new latch", itemType.getSimpleName(), identifier);
                }
            }

            // 락 획득 시도 (지수 백오프 적용)
            for (int attempt = 0; attempt < 3; attempt++) {
                // 각 시도마다 짧은 지연 추가 (스레드 간 경쟁 감소)
                try {
                    Thread.sleep(100 * (1 << attempt));  // 100ms, 200ms, 400ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (acquireLock(lockKey)) {
                    try {
                        // 락 획득 후 재확인
                        ApiCache apiCache = getApiCache(apiStatusKey);
                        if (isCachedDone(apiCache)) {
                            List<T> cachedResult = getFromCache(cacheKey, itemType);
                            if (cachedResult != null) {
                                List<T> filtered = cachedResult.stream().filter(nameFilter).toList();
                                if (!filtered.isEmpty()) {
                                    log.info("[Cache Hit after Lock] {}({}) - Got result after acquiring lock",
                                            itemType.getSimpleName(), identifier);
                                    return filtered;
                                }
                            }
                        }

                        // 직접 데이터 조회
                        log.info("[Direct Fetch] {}({}) - No cache, retrieving directly", itemType.getSimpleName(), identifier);
                        setPending(apiStatusKey);
                        List<T> result = retriever.get();
                        setCache(cacheKey, result, itemType);
                        setDone(apiStatusKey);

                        return result.stream().filter(nameFilter).toList();
                    } finally {
                        lockRedisTemplate.delete(lockKey);
                        if (latchInitialized) {
                            latch.countDown();  // 래치가 있으면 카운트다운
                        }
                    }
                }
            }

            // 모든 시도 실패, 최후의 수단으로 직접 조회 (로깅 강화)
            log.warn("[Last Resort] {}({}) - All attempts to acquire lock failed, direct retrieval",
                    itemType.getSimpleName(), identifier);
            List<T> result = retriever.get();
            return result.stream().filter(nameFilter).toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("대기 중 인터럽트 발생", e);
        } finally {
            // 자신이 초기화한 래치만 삭제 조건 유지
            if (latchInitialized) {
                try {
                    redissonClient.getCountDownLatch(latchKey).delete();
                } catch (Exception e) {
                    log.warn("[Latch Cleanup Failed] {}({}) - Failed to delete latch: {}",
                            itemType.getSimpleName(), identifier, e.getMessage());
                }
            }
        }
    }
    private <T> void setCache(String cacheKey, List<T> items, Class<T> itemType) {

        log.debug("[Cache Set] Starting to set cache for key: {}, type: {}, items count: {}",
                cacheKey, itemType.getSimpleName(), items != null ? items.size() : 0);

        RedisTemplate<String, List<T>> template = getTemplate(itemType);
        if (template != null) {
            try {
                template.opsForValue().set(cacheKey, items, CACHE_TTL);
                log.info("[Cache Set Success] Key: {}, type: {}, items: {}, TTL: {}s",
                        cacheKey, itemType.getSimpleName(), items.size(), CACHE_TTL.getSeconds());
            } catch (Exception e) {
                log.error("[Cache Set Failed] Failed to set cache for key: {}, type: {}, error: {}",
                        cacheKey, itemType.getSimpleName(), e.getMessage(), e);
            }
        } else {
            log.warn("[Cache Template Missing] RedisTemplate not found for type: {}, cache key: {}",
                    itemType.getSimpleName(), cacheKey);
        }
    }

    private <T> List<T> getFromCache(String cacheKey, Class<T> itemType) {
        log.debug("[Cache Get] Attempting to get from cache for key: {}, type: {}",
                cacheKey, itemType.getSimpleName());

        RedisTemplate<String, List<T>> template = getTemplate(itemType);

        if (template != null) {
            try {
                List<T> result = template.opsForValue().get(cacheKey);
                if (result != null) {
                    log.info("[Cache Get Success] Key: {}, type: {}, items found: {}",
                            cacheKey, itemType.getSimpleName(), result.size());
                    return result;
                } else {
                    log.info("[Cache Miss] No data found for key: {}, type: {}",
                            cacheKey, itemType.getSimpleName());
                }
            } catch (Exception e) {
                log.error("[Cache Get Failed] Failed to get from cache for key: {}, type: {}, error: {}",
                        cacheKey, itemType.getSimpleName(), e.getMessage(), e);
            }
        } else {
            log.warn("[Cache Template Missing] RedisTemplate not found for type: {}, cache key: {}",
                    itemType.getSimpleName(), cacheKey);
        }

        return Collections.emptyList();
    }

    private <T> RedisTemplate<String, List<T>> getTemplate(Class<T> itemType) {
        String type = switch (itemType.getSimpleName()) {
            case "AdAccount" -> "adAccount";
            default -> itemType.getSimpleName().toLowerCase();
        };

        return (RedisTemplate<String, List<T>>) redisTemplates.get(type);
    }

    private void setPending(String key) {
        try {
            // 기존 값이 없을 때만 설정이 아닌, 항상 설정으로 변경
            apiCacheRedisTemplate.opsForValue().set(
                    key,
                    ApiCache.pending(),
                    CACHE_TTL
            );
            log.debug("[Set Pending] {} - Status set to PENDING", key);
        } catch (Exception e) {
            log.warn("[Set Pending Failed] {} - Failed to set pending status: {}", key, e.getMessage());
        }
    }

    private void setDone(String key) {
        try {
            apiCacheRedisTemplate.opsForValue().set(
                    key,
                    ApiCache.done(),
                    CACHE_TTL
            );

            // 래치 처리 개선
            String latchKey = "latch:" + key.replace("apiStatus:", "");
            RCountDownLatch latch = redissonClient.getCountDownLatch(latchKey);

            // 래치가 존재하고 카운트가 있는 경우에만 카운트다운
            if (latch.getCount() > 0) {
                try {
                    latch.countDown();
                    log.debug("[Latch Countdown] {} - Successfully counted down latch", latchKey);
                } catch (Exception e) {
                    log.warn("[Latch Countdown Failed] {} - Failed to countdown: {}", latchKey, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[SetDone Failed] {} - Failed to set done status: {}", key, e.getMessage());
        }
    }

    private void delete(String key) {
        log.info("캐시 삭제 : {}", key);
        apiCacheRedisTemplate.delete(key);
    }

    public <T> List<T> getAndForceCaching(String identifier, Supplier<String> fetcher, Class<T> itemType)
            throws ExecutionException, InterruptedException, JsonProcessingException {

        String cacheKey = "apiResult:" + identifier;
        log.info("[Force Caching] Starting forced caching operation for {}, type: {}",
                identifier, itemType.getSimpleName());

        // 기존 캐시 조회
        List<T> cache = getFromCache(cacheKey, itemType);
        log.debug("[Force Caching] Found existing cache with {} items for {}", cache.size(), identifier);


        List<T> mutableCache;
        if (cache.isEmpty()) {
            log.debug("[Force Caching] No existing cache found, creating new list for {}", identifier);
            mutableCache = new ArrayList<>();
        } else {
            log.debug("[Force Caching] Found existing cache with {} items for {}", cache.size(), identifier);
            mutableCache = new ArrayList<>(cache); // 수정 가능한 새 리스트로 복사
        }

        // API 호출
        log.debug("[Force Caching] Invoking fetcher for {}", identifier);
        String response;
        try {
            response = fetcher.get();
            log.debug("[Force Caching] Fetcher returned response for {}: {} characters",
                    identifier, response.length());
        } catch (Exception e) {
            log.error("[Force Caching] Fetcher failed for {}: {}", identifier, e.getMessage(), e);
            throw e;
        }

        // 응답 파싱
        T item;
        try {
            item = objectMapper.readValue(response, itemType);
            log.info("[Force Caching] Successfully parsed response to {} for {}",
                    itemType.getSimpleName(), identifier);
        } catch (JsonProcessingException e) {
            log.error("[Force Caching] Failed to parse response for {}: {}", identifier, e.getMessage());
            log.debug("[Force Caching] Response content that failed parsing: {}", response);
            throw e;
        }

        // 캐시에 추가
        mutableCache.add(item);
        log.debug("[Force Caching] Added new item to cache, new size: {} for {}", mutableCache.size(), identifier);

        // 캐시 저장
        setCache(cacheKey, mutableCache, itemType);
        log.info("[Force Caching] Successfully completed forced caching for {}, cache size: {}",
                identifier, mutableCache.size());

        return mutableCache;
    }

    private boolean acquireLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 재시도 로직 추가
            for (int attempt = 0; attempt < 3; attempt++) {
                boolean acquired = lock.tryLock(100, 30, TimeUnit.SECONDS);
                if (acquired) {
                    log.debug("[Lock Acquired] {} - Lock acquired on attempt {}", lockKey, attempt + 1);
                    return true;
                }

                // 짧은 대기 후 재시도
                if (attempt < 2) {
                    Thread.sleep(50 * (1 << attempt));  // 50ms, 100ms 백오프
                }
            }
            log.debug("[Lock Failed] {} - Failed to acquire lock after 3 attempts", lockKey);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[Lock Interrupted] {} - Interrupted while acquiring lock", lockKey);
            return false;
        }
    }

    private static boolean isCachedDone(ApiCache apiCache) {
        return apiCache != null && apiCache.getApiStatus() == ApiStatus.DONE;
    }

    private ApiCache getApiCache(String key) {
        ApiCache apiCache = apiCacheRedisTemplate.opsForValue().get(key);
        log.info("apiCache 획득 여부 : {}", Objects.isNull(apiCache) ? "False" : apiCache.toString());
        return apiCache;
    }
}
