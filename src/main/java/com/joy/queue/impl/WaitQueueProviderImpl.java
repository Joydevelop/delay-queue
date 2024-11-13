package com.joy.queue.impl;

import com.joy.config.WaitQueueConfig;
import com.joy.entity.WaitQueueInfo;
import com.joy.queue.WaitQueueExecutor;
import com.joy.queue.WaitQueueProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Joy
 */
public class WaitQueueProviderImpl implements WaitQueueProvider {

    private ZSetOperations<String, Object> zSetOperations;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WaitQueueExecutor waitQueueExecutor;
    private final WaitQueueConfig waitQueueConfig;

    public WaitQueueProviderImpl(RedisTemplate<String, Object> redisTemplate, WaitQueueExecutor waitQueueExecutor) {
        this(redisTemplate, waitQueueExecutor, WaitQueueConfig.builder().searchTimeEnabled(false).build());
    }

    public WaitQueueProviderImpl(RedisTemplate<String, Object> redisTemplate, WaitQueueExecutor waitQueueExecutor, WaitQueueConfig waitQueueConfig) {
        this.redisTemplate = redisTemplate;
        this.waitQueueExecutor = waitQueueExecutor;
        this.waitQueueExecutor.init(this);
        this.waitQueueConfig = waitQueueConfig;
        if (null != redisTemplate) {
            this.zSetOperations = redisTemplate.opsForZSet();
        }
    }

    @Override
    public WaitQueueInfo enqueue(String queueName, String subject, Object extData) {
        if (null == zSetOperations || null == queueName || null == subject) {
            return noneQueueInfo();
        }
        String key = getZsetKey(queueName);
        WaitQueueInfo queueInfo = this.query(queueName, subject);
        if (queueInfo.getRank() != null && queueInfo.getRank() > -1) {
            //已入列
            return queueInfo;
        }
        long fromTime = System.currentTimeMillis();
        Boolean flag = zSetOperations.add(key, subject, (double) fromTime);
        if (Boolean.FALSE.equals(flag)) {
            return noneQueueInfo();
        }
        Long rank = zSetOperations.rank(key, subject);
        if (null == rank) {
            rank = 0L;
        }
        return WaitQueueInfo.builder().rank(rank).fromTime(fromTime).build();
    }

    private WaitQueueInfo noneQueueInfo() {
        return WaitQueueInfo.builder().rank(-1L).build();
    }

    @Override
    public WaitQueueInfo query(String queueName, String subject) {
        if (zSetOperations == null || queueName == null || subject == null) {
            return noneQueueInfo();
        }
        this.waitQueueExecutor.checkStatus(queueName);
        String key = getZsetKey(queueName);
        Long rank = zSetOperations.rank(key, subject);
        if (null == rank) {
            return noneQueueInfo();
        }
        long fromTime = System.currentTimeMillis();
        if (getSearchTimeEnabled()) {
            Double score = zSetOperations.score(key, subject);
            if (null != score && score > 0) {
                fromTime = score.longValue();
            }
        }
        return WaitQueueInfo.builder().rank(rank).fromTime(fromTime).build();
    }

    @Override
    public String peek(String queueName) {
        if (zSetOperations == null || queueName == null) {
            return null;
        }
        Set<Object> list = zSetOperations.range(getZsetKey(queueName), 0L, 0);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Iterator<Object> iterator = list.iterator();
        if (iterator.hasNext()) {
            Object key = iterator.next();
            return null != key ? key.toString() : null;
        }
        return null;
    }

    @Override
    public void remove(String queueName, String subject) {
        if (zSetOperations == null || queueName == null || subject == null) {
            return;
        }
        zSetOperations.remove(getZsetKey(queueName), subject);
    }

    @Override
    public void delete(String queueName) {
        if (zSetOperations == null || queueName == null) {
            return;
        }
        this.waitQueueExecutor.remove(queueName);
        redisTemplate.delete(getZsetKey(queueName));
    }

    @Override
    public Integer getSize(String queueName) {
        if (zSetOperations == null || queueName == null) {
            return 0;
        }
        Long value = zSetOperations.count(getZsetKey(queueName), 0, -1);
        return value == null ? 0 : value.intValue();
    }

    private String getZsetKey(String queueName) {
        if (null != waitQueueConfig && null != waitQueueConfig.getWaitQueueType()) {
            return waitQueueConfig.getWaitQueueType() + "_" + queueName;
        }
        return queueName;
    }

    public Boolean getSearchTimeEnabled() {
        if (null != waitQueueConfig) {
            return waitQueueConfig.getSearchTimeEnabled();
        }
        return true;
    }
}