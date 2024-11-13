package com.joy.queue;

/**
 * @author Joy
 */
public interface WaitQueueExecutor {
    /**
     * 初始化轮训服务
     *
     * @param waitQueueProvider
     */
    void init(WaitQueueProvider waitQueueProvider);

    /**
     * 验证队列状态
     *
     * @param queueName
     */
    void checkStatus(String queueName);

    /**
     * 删除队列
     *
     * @param queueName
     */
    void remove(String queueName);

    void stop();
}