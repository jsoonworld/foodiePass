package foodiepass.server.cache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String OCR_PREFIX = "ocr:";
    private static final String TRANSLATION_PREFIX = "translation:";
    private static final String FOOD_PREFIX = "food:";
    private static final String MENU_PREFIX = "menu:";

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }

            log.debug("Cache hit for key: {}", key);
            T result = objectMapper.readValue(value, type);
            return Optional.of(result);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached value for key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttl);
            log.debug("Cached value for key: {} with TTL: {}", key, ttl);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize value for key: {}", key, e);
        }
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
        log.debug("Evicted cache for key: {}", key);
    }

    @Override
    public void clear() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        log.info("Cleared all cache");
    }

    @Override
    public String buildOcrCacheKey(String imageHash) {
        return OCR_PREFIX + imageHash;
    }

    @Override
    public String buildTranslationCacheKey(String textHash, String targetLang) {
        return TRANSLATION_PREFIX + textHash + ":" + targetLang;
    }

    @Override
    public String buildFoodCacheKey(String dishName, String language) {
        return FOOD_PREFIX + dishName + ":" + language;
    }

    @Override
    public String buildMenuCacheKey(String scanHash) {
        return MENU_PREFIX + scanHash;
    }
}
