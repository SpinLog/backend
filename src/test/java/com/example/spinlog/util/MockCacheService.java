package com.example.spinlog.util;

import com.example.spinlog.global.cache.CacheService;

import java.util.HashMap;
import java.util.Map;

public class MockCacheService implements CacheService {
    Map<String, Map<String, Object>> cache = new HashMap<>();
    @Override
    public void putDataInHash(String key, String hashKey, Object data) {
        Map<String, Object> objectMap = cache.computeIfAbsent(key, k -> new HashMap<>());
        objectMap.put(hashKey, data);
    }

    @Override
    public void incrementDataInHash(String key, String hashKey, long delta) {
        Object dataFromHash = getDataFromHash(key, hashKey);
        if(!(dataFromHash instanceof Long)) {
            throw new IllegalArgumentException("Data is not Double type");
        }

        Long value = (Long) dataFromHash;
        putDataInHash(key, hashKey, value + delta);
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
        Map<String, Object> map = cache.get(key);
        if(map == null) {
            return null;
        }
        return map.get(hashKey);
    }

    @Override
    public Map<String, Object> getHashEntries(String key) {
        return cache.get(key);
    }

    @Override
    public void putAllDataInHash(String key, Map<String, Object> data) {
        cache.put(key, data);
    }

    @Override
    public void deleteHashKey(String key, String hashKey) {
        cache.get(key).remove(hashKey);
    }

    public void clear(){
        cache.clear();
    }
}
