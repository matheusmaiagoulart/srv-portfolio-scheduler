package com.matheus.srv_portfolio_scheduler.adapters.output.queries;

import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.RedisCachePort;
import com.matheus.srv_portfolio_scheduler.infrastructure.config.RedisPrefixesProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheAdapter implements RedisCachePort {

    private final ObjectMapper objectMapper;
    private final RedisPrefixesProps redisPrefixesProps;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public <T> Optional<T> get(String key, Class<T> desserializationClass) {
        log.info("Fetching from Redis cache with key: {}", key);
        Object result = null;
        try {
            result = redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failure while fetching key: {} cause: {}", key, e.getMessage());
        }

        if (result == null) {
            log.info("No cache found for key: {}", key);
            return Optional.empty();
        }

        try {
            T response = objectMapper.readValue(result.toString(), desserializationClass);
            log.info("Returning cached value for key: {}", key);
            return Optional.of(response);
        } catch (Exception e) {
            log.error("Error deserializing Redis cache value for key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void save(String key, Object value, int cacheDurationHours) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, cacheDurationHours, TimeUnit.HOURS);
            log.info("Saved to Redis cache with key: {}", key);
        } catch (Exception e) {
            log.error("Error saving to Redis cache with key: {}", key, e);
        }
    }

    @Override
    public void invalidateCacheForCustomersPortfolios() {
        String prefix = redisPrefixesProps.getCustomerPortfolioPrefix();
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(prefix + "*")
                .count(1000)
                .build();

        try {
            long deletedKeys = 0;
            List<String> keysToDelete = new ArrayList<>(1000);

            try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
                while (cursor.hasNext()) {
                    keysToDelete.add(cursor.next());

                    if (keysToDelete.size() == 1000) {
                        deletedKeys += deleteKeys(keysToDelete);
                    }
                }
            }

            deletedKeys += deleteKeys(keysToDelete);
            log.info("Invalidated {} Redis cache keys with prefix: {}", deletedKeys, prefix);
        } catch (Exception e) {
            log.error("Error invalidating Redis cache keys with prefix: {}", prefix, e);
        }
    }

    private long deleteKeys(List<String> keys) {
        if (keys.isEmpty()) return 0;

        Long deletedKeys = redisTemplate.delete(new ArrayList<>(keys));
        keys.clear();
        return deletedKeys == null ? 0 : deletedKeys;
    }
}
