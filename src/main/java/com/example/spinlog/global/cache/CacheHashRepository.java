package com.example.spinlog.global.cache;

import java.util.Map;

public interface CacheHashRepository {
    void putDataInHash(String key, String hashKey, Object data);

    void incrementDataInHash(String key, String hashKey, long delta);

    void decrementDataInHash(String key, String hashKey, long delta);

    void incrementDataInHash(String key, String hashKey, double delta);

    void decrementDataInHash(String key, String hashKey, double delta);

    Object getDataFromHash(String key, String hashKey);

    Map<String, Object> getHashEntries(String key);

    void putAllDataInHash(String key, Map<String, ?> data);

    void deleteHashKey(String key, String hashKey);

    void decrementAllDataInHash(String key, Map<String, ?> data);
    void incrementAllDataInHash(String key, Map<String, ?> data);

    void deleteData(String key);
}
