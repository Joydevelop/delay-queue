package com.joy.config;

import com.joy.queue.DefaultWaitQueueExecutor;
import com.joy.queue.WaitQueueProvider;
import com.joy.queue.impl.WaitQueueProviderImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Joy
 */
@Configuration
public class QueueConfiguration {

    @Bean
    public WaitQueueProvider waitQueueProvider(RedisTemplate<String,Object> redisTemplate,DefaultWaitQueueExecutor defaultWaitQueueExecutor) {
        return new WaitQueueProviderImpl(redisTemplate, defaultWaitQueueExecutor);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }


}