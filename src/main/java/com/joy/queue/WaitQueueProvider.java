package com.joy.queue;

import com.joy.entity.WaitQueueInfo;

/**
 * @author Joy
 */
public interface WaitQueueProvider {

    default WaitQueueInfo enqueue(String queueName, String subject) {
        return enqueue(queueName, subject, null);
    }

    /**
     * 入列
     *
     * @param queueName 队列名称
     * @param subject   排队数据
     * @param extData   扩展数据
     * @return 排队情况
     */
    WaitQueueInfo enqueue(String queueName, String subject, Object extData);

    /**
     * 查询排队情况
     *
     * @param queueName 队列名称
     * @param subject   排队数据
     * @return 排队情况
     */
    WaitQueueInfo query(String queueName, String subject);

    /**
     * 获取排队主题
     *
     * @param queueName 队列名称
     * @return 需要处理的业务
     */
    String peek(String queueName);

    /**
     * 移除指定排队信息
     *
     * @param queueName 队列名称
     * @param subject   排队数据
     */
    void remove(String queueName, String subject);

    /**
     * 删除队列
     *
     * @param queueName 队列名称
     */
    void delete(String queueName);

    /**
     * 获取队列长度
     *
     * @param queueName 队列名称
     * @return 队列长度
     */
    Integer getSize(String queueName);
}
