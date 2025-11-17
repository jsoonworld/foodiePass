package foodiepass.server.cache.service;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {

    <T> Optional<T> get(String key, Class<T> type);

    void put(String key, Object value, Duration ttl);

    void evict(String key);

    void clear();

    // Cache key builders for different layers
    String buildOcrCacheKey(String imageHash);
    String buildTranslationCacheKey(String textHash, String targetLang);
    String buildFoodCacheKey(String dishName, String language);
    String buildMenuCacheKey(String scanHash);
}
