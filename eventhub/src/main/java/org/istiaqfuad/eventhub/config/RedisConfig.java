package org.istiaqfuad.eventhub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Redis configuration: cache manager with per-cache TTLs and a shared
 * {@link StringRedisTemplate} used by rate limiting and the waiting room.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf, ObjectMapper objectMapper) {
        // GenericJackson2JsonRedisSerializer(ObjectMapper) is the recommended non-deprecated form
        // in Spring Data Redis 4.x for cache serialization.
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaults)
                .withCacheConfiguration("events",
                        defaults.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("venues",
                        defaults.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("categories",
                        defaults.entryTtl(Duration.ofHours(1)))
                .build();
    }

    /**
     * Shared template for rate-limiting and waiting-room operations.
     * Uses String serialization — values are simple counters or flags.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }
}

