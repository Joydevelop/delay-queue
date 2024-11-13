# 基于Redis实现延时队列

## 集成方式

### 1、实现`com.joy.queue.AbstractWaitQueueExecutor`

```java

@Setter
@Getter
@Slf4j
@Component
public class DefaultWaitQueueExecutor extends AbstractWaitQueueExecutor {

    private BiFunction<String, String, Boolean> consumer;

    public DefaultWaitQueueExecutor(ThreadPoolTaskExecutor taskExecutor) {
        super(taskExecutor);
    }

    @Override
    public boolean processByQueueName(String queueName, String subject, Object extData) {
        log.debug("队列运行程序 queueName:{} subject:{}", queueName, subject);
        if (null != getConsumer()) {
            return getConsumer().apply(queueName, subject);
        }
        return false;
    }
}
```

### 2.调用`com.joy.queue.WaitQueueProvider#enqueue`入列

```java
    WaitQueueInfo waitQueueInfo1 = waitQueueProvider.enqueue(QUEUE_NAME, "test1");
```

### 3.调用任意队列名的`com.joy.queue.WaitQueueProvider#query`即可启动队列

```java
        WaitQueueInfo waitQueueInfo = waitQueueProvider.query(QUEUE_NAME, "test1");
```
