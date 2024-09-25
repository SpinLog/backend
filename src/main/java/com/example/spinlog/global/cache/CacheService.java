package com.example.spinlog.global.cache;

import org.springframework.data.redis.core.HashOperations;

import java.util.Map;

public interface CacheService {
    void putDataInHash(String key, String hashKey, Object data);

    void incrementDataInHash(String key, String hashKey, long delta);

    void decrementDataInHash(String key, String hashKey, long delta);

    void incrementDataInHash(String key, String hashKey, double delta);

    void decrementDataInHash(String key, String hashKey, double delta);

    Object getDataFromHash(String key, String hashKey);

    Map<String, Object> getHashEntries(String key);

    void putAllDataInHash(String key, Map<String, Object> data);
}
