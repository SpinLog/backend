package com.example.spinlog.global.cache;

import com.example.spinlog.statistics.exception.InvalidCacheException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheHashRepository implements CacheHashRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void putDataInHash(String key, String hashKey, Object data) {
        redisTemplate.opsForHash().put(key, hashKey, data.toString());
    }

    @Override
    public void incrementDataInHash(String key, String hashKey, long delta) {
        redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    @Override
    public void decrementDataInHash(String key, String hashKey, long delta) {
        redisTemplate.opsForHash().increment(key, hashKey, -delta);
    }

    @Override
    public void incrementDataInHash(String key, String hashKey, double delta) {
        redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    public void deleteHashKey(String key, String hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }

    @Override
    public void decrementDataInHash(String key, String hashKey, double delta) {
        redisTemplate.opsForHash().increment(key, hashKey, -delta);
    }

    @Override
    public Object getDataFromHash(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    @Override
    public Map<String, Object> getHashEntries(String key) {
        HashOperations<String, String, Object> opsForHash = redisTemplate.opsForHash();
        return opsForHash.entries(key);
    }

    @Override
    public void putAllDataInHash(String key, Map<String, ?> data) {
        Map<String, String> stringData = data.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()
                ));
        redisTemplate.opsForHash().putAll(key, stringData);
    }

    @Override
    public void decrementAllDataInHash(String key, Map<String, ?> data) {
        data.forEach((hashKey, value) -> {
            if (value instanceof Long) {
                decrementDataInHash(key, hashKey, (Long) value);
            } else if (value instanceof Double) {
                decrementDataInHash(key, hashKey, (Double) value);
            } else {
                log.error("Invalid data type: {}, key: {}, hashKey: {}, value: {}", value.getClass(), key, hashKey, value);
                throw new InvalidCacheException("Invalid data type: " + value.getClass());
            }
        });
    }

    @Override
    public void incrementAllDataInHash(String key, Map<String, ?> data) {
        data.forEach((hashKey, value) -> {
            if (value instanceof Long) {
                incrementDataInHash(key, hashKey, (Long) value);
            } else if (value instanceof Double) {
                incrementDataInHash(key, hashKey, (Double) value);
            } else {
                log.error("Invalid data type: {}, key: {}, hashKey: {}, value: {}", value.getClass(), key, hashKey, value);
                throw new InvalidCacheException("Invalid data type: " + value.getClass());
            }
        });
    }

    @Override
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}
