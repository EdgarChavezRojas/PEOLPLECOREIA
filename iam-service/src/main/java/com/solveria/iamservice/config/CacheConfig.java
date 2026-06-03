package com.solveria.iamservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class to enable Spring Caching and configure Redis Cache Manager.
 *
 * <p>Uses Jackson JSON serialization to store human-readable/portable cache values in Redis and
 * configures distinct TTL settings for different cache names.
 *
 * <p>Implements {@link CachingConfigurer} to define a resilient CacheErrorHandler that falls back
 * silently to the database if the Redis server becomes unavailable.
 *
 * <p><b>Production Activation Steps:</b>
 *
 * <ul>
 *   <li>1. Set <code>spring.data.redis.ssl.enabled=true</code> in <code>application-prod.yml</code>
 *       to encrypt data in transit.
 *   <li>2. If utilizing Redis Sentinel/Cluster, configure node endpoints under <code>
 *       spring.data.redis.sentinel</code> or <code>spring.data.redis.cluster</code>.
 *   <li>3. Provide Redis connection credentials using <code>SPRING_REDIS_PASSWORD</code>
 *       environment variable.
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String USERS_CACHE = "users";
    public static final String USERS_BY_EMAIL_CACHE = "usersByEmail";
    public static final String USERS_BY_USERNAME_CACHE = "usersByUsername";
    public static final String ROLES_CACHE = "roles";

    public static final String PERSONS_CACHE = "persons";
    public static final String PERSONS_BY_CI_CACHE = "personsByCi";
    public static final String PERSONS_BY_GLOBAL_ID_CACHE = "personsByGlobalId";
    public static final String PERSONS_BY_USER_ID_CACHE = "personsByUserId";

    public static final String RELATIONSHIPS_CACHE = "relationships";
    public static final String RELATIONSHIPS_BY_PERSON_CACHE = "relationshipsByPerson";
    public static final String POSITIONS_CACHE = "positions";

    public static final String TIMESHEET_PERIODS_CACHE = "timesheetPeriods";
    public static final String TIMESHEET_PERIODS_BY_ORG_UNIT_CACHE = "timesheetPeriodsByOrgUnit";
    public static final String CLOCKING_DEVICES_CACHE = "clockingDevices";
    public static final String CLOCKING_DEVICES_BY_ORG_UNIT_CACHE = "clockingDevicesByOrgUnit";

    public static final String SCHEDULE_PLANS_CACHE = "schedulePlans";

    public static final String PAYROLL_PERIODS_CACHE = "payrollPeriods";
    public static final String PAYROLL_PERIODS_BY_MONTH_YEAR_CACHE = "payrollPeriodsByMonthYear";
    public static final String PAYROLL_GROUPS_CACHE = "payrollGroups";
    public static final String PAYROLL_GROUPS_BY_CODE_CACHE = "payrollGroupsByCode";

    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration(ObjectMapper objectMapper) {
        // Configure Jackson Serializer for Redis values
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default TTL of 30 minutes
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                valueSerializer));
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheConfiguration defaultCacheConfiguration) {

        Map<String, RedisCacheConfiguration> customConfigs = new HashMap<>();

        // Custom TTL configurations (15 minutes for volatile/dynamic transactional data)
        customConfigs.put(USERS_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                USERS_BY_EMAIL_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                USERS_BY_USERNAME_CACHE,
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));

        customConfigs.put(ROLES_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofHours(1)));

        customConfigs.put(
                PERSONS_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                PERSONS_BY_CI_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                PERSONS_BY_GLOBAL_ID_CACHE,
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                PERSONS_BY_USER_ID_CACHE,
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));

        customConfigs.put(
                RELATIONSHIPS_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                RELATIONSHIPS_BY_PERSON_CACHE,
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                POSITIONS_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(30)));

        customConfigs.put(
                TIMESHEET_PERIODS_CACHE,
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                TIMESHEET_PERIODS_BY_ORG_UNIT_CACHE,
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                CLOCKING_DEVICES_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                CLOCKING_DEVICES_BY_ORG_UNIT_CACHE,
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));

        customConfigs.put(
                SCHEDULE_PLANS_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));

        customConfigs.put(
                PAYROLL_PERIODS_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                PAYROLL_PERIODS_BY_MONTH_YEAR_CACHE,
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                PAYROLL_GROUPS_CACHE, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        customConfigs.put(
                PAYROLL_GROUPS_BY_CODE_CACHE,
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfiguration)
                .withInitialCacheConfigurations(customConfigs)
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error(
                        "event=REDIS_CACHE_GET_ERROR cache={} key={} error={}",
                        cache.getName(),
                        key,
                        exception.getMessage());
            }

            @Override
            public void handleCachePutError(
                    RuntimeException exception, Cache cache, Object key, Object value) {
                log.error(
                        "event=REDIS_CACHE_PUT_ERROR cache={} key={} error={}",
                        cache.getName(),
                        key,
                        exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error(
                        "event=REDIS_CACHE_EVICT_ERROR cache={} key={} error={}",
                        cache.getName(),
                        key,
                        exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error(
                        "event=REDIS_CACHE_CLEAR_ERROR cache={} error={}",
                        cache.getName(),
                        exception.getMessage());
            }
        };
    }
}
