package com.example.spinlog.util;

import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Primary
@Profile("test")
public class MockHashCacheService implements HashCacheService {
    Map<String, Map<String, Object>> cache = new HashMap<>();
    @Override
    public void putDataInHash(String key, String hashKey, Object data) {
        Map<String, Object> objectMap = cache.computeIfAbsent(key, k -> new HashMap<>());
        objectMap.put(hashKey, data);
    }

    @Override
    public void incrementDataInHash(String key, String hashKey, long delta) {
        Object dataFromHash = getDataFromHash(key, hashKey);
        if(dataFromHash == null) {
            putDataInHash(key, hashKey, delta);
            return;
        }

        if(!(dataFromHash instanceof Long)) {
            throw new InvalidCacheException("Data is not Long type");
        }

        Long value = (Long) dataFromHash;
        putDataInHash(key, hashKey, value + delta);
    }

    @Override
    public void decrementDataInHash(String key, String hashKey, long delta) {
        Object dataFromHash = getDataFromHash(key, hashKey);

        if(!(dataFromHash instanceof Long)) {
            throw new InvalidCacheException("Data is not Long type");
        }

        Long value = (Long) dataFromHash;
        cache.get(key).put(hashKey, value - delta);
    }

    @Override
    public void incrementDataInHash(String key, String hashKey, double delta) {
        Object dataFromHash = getDataFromHash(key, hashKey);
        if(dataFromHash == null) {
            putDataInHash(key, hashKey, delta);
            return;
        }

        if(!(dataFromHash instanceof Double)) {
            throw new InvalidCacheException("Data is not Double type");
        }

        Double value = (Double) dataFromHash;
        cache.get(key).put(hashKey, value + delta);
    }

    @Override
    public void decrementDataInHash(String key, String hashKey, double delta) {
        Object dataFromHash = getDataFromHash(key, hashKey);
        if(!(dataFromHash instanceof Double)) {
            throw new InvalidCacheException("Data is not Double type");
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

    @Override
    public void decrementAllDataInHash(String key, Map<String, Object> data) {
        data.forEach((hashKey, value) -> {
            if (value instanceof Long) {
                decrementDataInHash(key, hashKey, (Long) value);
            } else if (value instanceof Double) {
                decrementDataInHash(key, hashKey, (Double) value);
            }
        });
    }

    @Override
    public void incrementAllDataInHash(String key, Map<String, Object> data) {
        data.forEach((hashKey, value) -> {
            if (value instanceof Long) {
                incrementDataInHash(key, hashKey, (Long) value);
            } else if (value instanceof Double) {
                incrementDataInHash(key, hashKey, (Double) value);
            }
        });
    }
}
