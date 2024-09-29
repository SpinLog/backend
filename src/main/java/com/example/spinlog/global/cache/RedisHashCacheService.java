package com.example.spinlog.global.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisHashCacheService implements HashCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void putDataInHash(String key, String hashKey, Object data) {
        redisTemplate.opsForHash().put(key, hashKey, data);
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

    // todo 예외 처리 - ex) key가 없을 때, hashKey가 없을 때, 캐스팅 에러 등
    @Override
    public Map<String, Object> getHashEntries(String key) {
        HashOperations<String, String, Object> opsForHash = redisTemplate.opsForHash();
        return opsForHash.entries(key);
    }

    @Override
    public void putAllDataInHash(String key, Map<String, Object> data) {
        redisTemplate.opsForHash().putAll(key, data);
    }

}
