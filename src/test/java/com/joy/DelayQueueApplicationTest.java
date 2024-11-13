package com.joy;

import com.joy.entity.WaitQueueInfo;
import com.joy.queue.WaitQueueProvider;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Joy
 */
@SpringBootTest
public class DelayQueueApplicationTest {
    @Resource
    private WaitQueueProvider waitQueueProvider;

    private static final String QUEUE_NAME = "test_queue";


    @Test
    public void test() {
        WaitQueueInfo waitQueueInfo1 = waitQueueProvider.enqueue(QUEUE_NAME, "test1");
        WaitQueueInfo waitQueueInfo2 = waitQueueProvider.enqueue(QUEUE_NAME, "test2");
        WaitQueueInfo waitQueueInfo3 = waitQueueProvider.enqueue(QUEUE_NAME, "test3");

    }
}
