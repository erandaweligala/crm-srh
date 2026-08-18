package com.adl.et.telco.crm.securerequesthandler.application.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Configuration class for Redis setup.
 * Configures Redis connection factory, templates, and serializers with proper logging.
 */
@Configuration
public class RedisConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    private static final String LOG_PREFIX = "SRH|RedisConfig|";

    @Value("${spring.redis.host}")
    private String redisHostName;

    @Value("${spring.redis.port}")
    private int redisPort;

    /**
     * Creates Redis standalone configuration.
     * @return Configured RedisStandaloneConfiguration instance
     */
    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        logger.debug("{}redisStandaloneConfiguration|Start|Host: {}|Port: {}", 
            LOG_PREFIX, redisHostName, redisPort);
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHostName);
        config.setPort(redisPort);
        logger.info("{}redisStandaloneConfiguration|End|Configuration created", LOG_PREFIX);
        return config;
    }

    /**
     * Creates Jedis connection factory.
     * @return Configured JedisConnectionFactory instance
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        logger.debug("{}jedisConnectionFactory|Start", LOG_PREFIX);
        JedisConnectionFactory factory = new JedisConnectionFactory(redisStandaloneConfiguration());
        logger.info("{}jedisConnectionFactory|End|Factory created", LOG_PREFIX);
        return factory;
    }

    /**
     * Creates a Redis template for string operations.
     * @return Configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, String> stringRedisTemplate() {
        logger.debug("{}stringRedisTemplate|Start", LOG_PREFIX);
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setDefaultSerializer(new StringRedisSerializer());
        logger.info("{}stringRedisTemplate|End|Template created", LOG_PREFIX);
        return template;
    }

    /**
     * Creates a Redis template for string operations with explicit serializers.
     * @return Configured RedisTemplate instance
     */
    @Bean
    @Primary
    public RedisTemplate<String, String> stringRedisTemplateWithExplicitSerializers() {
        logger.debug("{}stringRedisTemplateWithExplicitSerializers|Start", LOG_PREFIX);
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
        template.setValueSerializer(stringRedisSerializer);
        template.setKeySerializer(stringRedisSerializer);
        logger.info("{}stringRedisTemplateWithExplicitSerializers|End|Template created", LOG_PREFIX);
        return template;
    }

    /**
     * Creates a Redis template for map operations.
     * @param connectionFactory The Redis connection factory
     * @return Configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Map<Integer, String>> mapRedisTemplate(RedisConnectionFactory connectionFactory) {
        logger.debug("{}mapRedisTemplate|Start", LOG_PREFIX);
        RedisTemplate<String, Map<Integer, String>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        template.setHashKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        template.setValueSerializer(mapValueSerializer());
        template.setHashValueSerializer(mapValueSerializer());

        template.afterPropertiesSet();
        logger.info("{}mapRedisTemplate|End|Template created", LOG_PREFIX);
        return template;
    }

    /**
     * Creates a custom serializer for Map<Integer, String>.
     * @return Configured RedisSerializer instance
     */
    private RedisSerializer<Map<Integer, String>> mapValueSerializer() {
        logger.debug("{}mapValueSerializer|Start", LOG_PREFIX);
        return new RedisSerializer<Map<Integer, String>>() {
            private final ObjectMapper objectMapper = new ObjectMapper();

            @Override
            public byte[] serialize(Map<Integer, String> map) {
                try {
                    return objectMapper.writeValueAsBytes(map);
                } catch (Exception e) {
                    logger.error("{}mapValueSerializer|SerializeError|Error: {}", 
                        LOG_PREFIX, e.getMessage(), e);
                    return null;
                }
            }

            @Override
            public Map<Integer, String> deserialize(byte[] bytes) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                try {
                    return objectMapper.readValue(bytes, new TypeReference<Map<Integer, String>>() {});
                } catch (Exception e) {
                    logger.error("{}mapValueSerializer|DeserializeError|Error: {}", 
                        LOG_PREFIX, e.getMessage(), e);
                    return null;
                }
            }
        };
    }
}
