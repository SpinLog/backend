package com.example.spinlog.statistics.service.caching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void putDataInHash(String key, String hashKey, Object data) {
        redisTemplate.opsForHash().put(key, hashKey, data);
    }

    public Object getDataFromHash(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    // todo 예외 처리 - ex) key가 없을 때, hashKey가 없을 때, 캐스팅 에러 등
    public Map<String, Object> getHashEntries(String key) {
        HashOperations<String, String, Object> opsForHash = redisTemplate.opsForHash();
        return opsForHash.entries(key);
    }

    public void putAllDataInHash(String key, Map<String, Object> data) {
        redisTemplate.opsForHash().putAll(key, data);
    }

}
