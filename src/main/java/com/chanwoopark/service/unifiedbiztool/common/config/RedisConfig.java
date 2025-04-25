package com.chanwoopark.service.unifiedbiztool.common.config;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.AdAccount;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.Campaign;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.Page;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.Set;
import com.chanwoopark.service.unifiedbiztool.api.model.entity.ApiCache;
import com.chanwoopark.service.unifiedbiztool.common.model.entity.PlatformToken;
import com.chanwoopark.service.unifiedbiztool.post.model.entity.Post;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    private final ObjectMapper objectMapper;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
        redisStandaloneConfiguration.setPassword(password);

        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, Post> postRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Post> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        Jackson2JsonRedisSerializer<Post> serializer = new Jackson2JsonRedisSerializer<>(Post.class);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }

    @Bean
    public RedisTemplate<String, PlatformToken> platformTokenRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, PlatformToken> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        Jackson2JsonRedisSerializer<PlatformToken> serializer = new Jackson2JsonRedisSerializer<>(PlatformToken.class);
        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }

    @Bean
    public RedisTemplate<String, ApiCache> apiCacheRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, ApiCache> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        Jackson2JsonRedisSerializer<ApiCache> serializer = new Jackson2JsonRedisSerializer<>(ApiCache.class);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }

    @Bean
    public RedisTemplate<String, String> lockRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    public <T> RedisTemplate<String, List<T>> genericListRedisTemplate(RedisConnectionFactory factory, Class<T> type) {
        RedisTemplate<String, List<T>> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, type);
        Jackson2JsonRedisSerializer<List<T>> serializer = new Jackson2JsonRedisSerializer<>(javaType);

        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisTemplate<String, List<AdAccount>> adAccount(RedisConnectionFactory factory) {
        return genericListRedisTemplate(factory, AdAccount.class);
    }

    @Bean
    public RedisTemplate<String, List<Campaign>> campaign(RedisConnectionFactory factory) {
        return genericListRedisTemplate(factory, Campaign.class);
    }

    @Bean
    public RedisTemplate<String, List<Set>> set(RedisConnectionFactory factory) {
        return genericListRedisTemplate(factory, Set.class);
    }

    @Bean
    public RedisTemplate<String, List<Page>> page(RedisConnectionFactory factory) {
        return genericListRedisTemplate(factory, Page.class);
    }
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(2)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        if (!Objects.equals(password, "") && password != null) {
            config.useSingleServer().setPassword(password);
        }

        return Redisson.create(config);
    }



}
