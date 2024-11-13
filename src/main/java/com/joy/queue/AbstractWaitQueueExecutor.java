package com.joy.queue;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Joy
 */
@Slf4j
public abstract class AbstractWaitQueueExecutor implements WaitQueueExecutor {


    private final Map<String, Integer> queueSet = new ConcurrentHashMap<>();

    private final ThreadPoolTaskExecutor taskExecutor;

    private volatile boolean isRunning = false;

    @Setter
    private Long waitInterval = 30 * 1000L;

    @Setter
    private Integer failFitCount = 100;

    private Future<?> threadFuture;


    private WaitQueueProvider waitQueueProvider;

    protected String getServiceName() {
        return "排队监听线程处理程序";
    }

    public AbstractWaitQueueExecutor(@Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void init(WaitQueueProvider waitQueueProvider) {
        this.waitQueueProvider = waitQueueProvider;
    }

    @Override
    public void checkStatus(String queueName) {
        queueSet.put(queueName, 0);
        notifySingle();
        start();
    }

    @Override
    public void remove(String queueName) {
        queueSet.remove(queueName);
    }

    @Override
    public synchronized void stop() {
        this.isRunning = false;
        if (threadFuture != null) {
            try {
                threadFuture.cancel(true);
            } catch (Throwable throwable) {
                //do something
            }
        }
    }

    public synchronized void start() {
        if (null == waitQueueProvider) {
            return;
        }
        if (isRunning) {
            return;
        }
        this.isRunning = true;
        threadFuture = taskExecutor.submit(this::run);
    }

    public void run() {
        while (isRunning) {
            try {
                runInternalSingle();
            } catch (Throwable throwable) {
                //do something
            }
        }
    }

    protected void notifySingle() {
        synchronized (this) {
            this.notifyAll();
        }
    }

    protected void waitSingle() throws InterruptedException {
        synchronized (this) {
            this.wait(getWaitInterval());
        }
    }

    private void runInternalSingle() throws InterruptedException {
        List<String> tmpList = new LinkedList<>(queueSet.keySet());
        if (tmpList.isEmpty()) {
            this.waitSingle();
            return;
        }
        if (tmpList.size() == 1) {
            String queueName = tmpList.get(0);
            if (peekAndProcess(queueName, null)) {
                TimeUnit.MILLISECONDS.sleep(10L);
            }
            return;
        }
        log.debug("{}线程数据获取 队列名称长度：{}", getServiceName(), tmpList.size());
        CountDownLatch startCount = new CountDownLatch(tmpList.size());
        boolean hasFetch = false;
        for (String queueName : tmpList) {
            if (peekAndProcess(queueName, startCount)) {
                hasFetch = true;
            }
        }
        if (!hasFetch) {
            this.waitSingle();
        } else {
            TimeUnit.MILLISECONDS.sleep(10L);
        }
    }

    protected boolean peekAndProcess(String queueName, CountDownLatch startCount) throws InterruptedException {
        boolean hasFetch = false;
        try {
            String subject = waitQueueProvider.peek(queueName);
            log.debug("{}线程数据获取 队列名称：{} 队列第一个元素：{}", getServiceName(), queueName, subject);
            if (null != subject) {
                hasFetch = true;
                if (processByQueueName(queueName, subject, null)) {
                    waitQueueProvider.remove(queueName, subject);
                } else {
                    TimeUnit.MILLISECONDS.sleep(100L);
                }
            } else {
                checkFaitCount(queueName);
            }
        } catch (Exception e) {
            //do something
        } finally {
            if (null != startCount) {
                startCount.countDown();
            } else if (!hasFetch) {
                this.waitSingle();
            }
        }
        return hasFetch;
    }

    public boolean processByQueueName(String queueName, String subject, Object extData) {
        return false;
    }

    protected void checkFaitCount(String queueName) {
        Integer count = queueSet.get(queueName);
        log.debug("{} 暂无排队数据，发生次数：{}", queueName, count);
        if (null == count) {
            count = 0;
        }
        if (count > getFailFitCount()) {
            queueSet.remove(queueName);
        } else {
            queueSet.put(queueName, count + 1);
        }
    }

    public Long getWaitInterval() {
        if (waitInterval == null || waitInterval <= 0L) {
            waitInterval = 5000L;
        }
        return waitInterval;
    }

    public Integer getFailFitCount() {
        if (failFitCount == null || failFitCount == 0) {
            failFitCount = 10;
        }
        return failFitCount;
    }
}