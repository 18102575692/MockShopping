package com.example.ssm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import javax.annotation.PostConstruct;

@Configuration
public class RedisConfig {
    @Autowired
    RedisTemplate redisTemplate;

    @PostConstruct
    public void initRedisTemplate(){
        RedisSerializer serializer = this.redisTemplate.getStringSerializer();
        redisTemplate.setKeySerializer(serializer);
        redisTemplate.setHashKeySerializer(serializer);
    }
}
