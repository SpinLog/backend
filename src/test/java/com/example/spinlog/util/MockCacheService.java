package com.example.spinlog.util;

import com.example.spinlog.global.cache.CacheService;

import java.util.Map;

public class MockCacheService implements CacheService {
    Map<String, Map<String, Object>> cache;
    @Override
    public void putDataInHash(String key, String hashKey, Object data) {
        cache.get(key).put(hashKey, data);
    }

    @Override
    public void incrementDataInHash(String key, String hashKey, long delta) {
        Object dataFromHash = getDataFromHash(key, hashKey);
        if(!(dataFromHash instanceof Long)) {
            throw new IllegalArgumentException("Data is not Double type");
        }

        Long value = (Long) dataFromHash;
        cache.get(key).put(hashKey, value + delta);
    }

    @Override
    public void decrementDataInHash(String key, String hashKey, long delta) {
        Object dataFromHash = getDataFromHash(key, hashKey);
        if(!(dataFromHash instanceof Long)) {
            throw new IllegalArgumentException("Data is not Double type");
        }

        Long value = (Long) dataFromHash;
        cache.get(key).put(hashKey, value - delta);
    }

    @Override
    public void incrementDataInHash(String key, String hashKey, double delta) {
        Object dataFromHash = getDataFromHash(key, hashKey);
        if(!(dataFromHash instanceof Double)) {
            throw new IllegalArgumentException("Data is not Double type");
        }

        Double value = (Double) dataFromHash;
        cache.get(key).put(hashKey, value + delta);
    }

    @Override
    public void decrementDataInHash(String key, String hashKey, double delta) {
        Object dataFromHash = getDataFromHash(key, hashKey);
        if(!(dataFromHash instanceof Double)) {
            throw new IllegalArgumentException("Data is not Double type");
        }

        Double value = (Double) dataFromHash;
        cache.get(key).put(hashKey, value - delta);
    }

    @Override
    public Object getDataFromHash(String key, String hashKey) {
        return cache.get(key).get(hashKey);
    }

    @Override
    public Map<String, Object> getHashEntries(String key) {
        return cache.get(key);
    }

    @Override
    public void putAllDataInHash(String key, Map<String, Object> data) {
        cache.put(key, data);
    }
}
