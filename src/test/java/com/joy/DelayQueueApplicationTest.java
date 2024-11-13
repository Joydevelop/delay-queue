package com.joy;

import com.joy.entity.WaitQueueInfo;
import com.joy.queue.DefaultWaitQueueExecutor;
import com.joy.queue.WaitQueueProvider;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Joy
 */
@SpringBootTest
@DisplayName("延迟队列测试")
public class DelayQueueApplicationTest {
    @Resource
    private WaitQueueProvider waitQueueProvider;
    @Resource
    private DefaultWaitQueueExecutor defaultWaitQueueExecutor;

    private static final String QUEUE_NAME = "test_queue";

    @Test
    @Tag("入列测试")
    public void testEnqueue() {
        WaitQueueInfo waitQueueInfo1 = waitQueueProvider.enqueue(QUEUE_NAME, "test1");
        WaitQueueInfo waitQueueInfo2 = waitQueueProvider.enqueue(QUEUE_NAME, "test2");
        WaitQueueInfo waitQueueInfo3 = waitQueueProvider.enqueue(QUEUE_NAME, "test3");
        Assertions.assertNotNull(waitQueueInfo1);
        Assertions.assertNotNull(waitQueueInfo2);
        Assertions.assertNotNull(waitQueueInfo3);
    }

    @Test
    @DisplayName("队列启动测试")
    public void testQuery() {
        defaultWaitQueueExecutor.setConsumer((queueName, subject) -> {
            System.out.println("queueName:" + queueName + " subject:" + subject);
            return true;
        });
        WaitQueueInfo waitQueueInfo = waitQueueProvider.query(QUEUE_NAME, "test1");
        Assertions.assertNotNull(waitQueueInfo);
    }
}
