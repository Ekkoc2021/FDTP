

### 核心部分



统一管理线程池的组件==>容器

- 核心:提供刷新线程池参数的接口
- 其他功能: 线程池监控接口
- 能够接管已有旧线程池: 接管旧线程池
  应用里面已经定义好的线程池,也能够在尽量少的修改源代码的情况下适配到容器==>比如说也能够管理tomcat/dubbo的线程池

> 创建线程池==>注册到线程池管理组件==>应用运行
>
> 根据业务和需求场景不重启应用动态刷新线程池参数 

动态线程池实现 : DT

> 具体实现动态刷新所有参数的功能
>
> 动态修改线程池实现的功能: 从普通的jdk线程池,其他的线程池实现

- 线程池类型: 普通jdk线程池,支持虚拟线程的线程池池,其他类型的线程池(disruptor实现)
  自定义一个可动态修改线程池参数的类 ==> 只要实现这个类都可以注册到线程池容器中
- 线程池拒接策略: 类似aop的机制,增强原有拒绝策略,告警机制等等

线程池类型

- JDK线程池: 核心参数设置方案,jdk提供有设置参数的接口
  阻塞队列动态伸缩问题
- 支持虚拟线程: 就是普通jdk的扩展性
- dsruptor实现的线程池: 需要封装成和普通线程池一样的参数,切换就采用直接替换掉原有线程池对象



- corePoolSize : 核心线程池的大小，如果核心线程池有空闲位置，这时新的任务就会被核心线程池新建一个线程执行，执行完毕后不会销毁线程，线程会进入缓存队列等待再次被运行。
- maximunPoolSize : 最大线程池数量,超过这个数量,新任务到达会放入阻塞队列
- workQueue : 阻塞队列,jdk队列不支持动态修改容量,需要解决
- 拒绝策略 :增强拒绝策略,一些告警机制通知通过拒接策略去实现

Disrutor

```
消费者发布事件 => disruptor执行对应实现的handler处理事件
```



```Java
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DeadlineTimerWheel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

// 1. 定义事件（消息模型）
class LongEvent {
    private long value;

    public void set(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }
}

// 2. 定义事件工厂
class LongEventFactory implements EventFactory<LongEvent> {
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}

// 3. 定义事件处理器（消费者）
class LongEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("Event: " + event.get() + " processed by " + Thread.currentThread().getName());
    }
}

// 4. 定义生产者
class Producer {
    private final RingBuffer<LongEvent> ringBuffer;

    public Producer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(long value) {
        // 1. 获取下一个可用的序号
        long sequence = ringBuffer.next();
        try {
            // 2. 获取该序号对应的事件
            LongEvent event = ringBuffer.get(sequence);
            // 3. 设置事件数据
            event.set(value);
        } finally {
            // 4. 发布事件（必须在finally中确保发布）
            ringBuffer.publish(sequence);
        }
    }
}

// 5. 主程序
public class DisruptorDemo {
    public static void main(String[] args) throws InterruptedException {
        // RingBuffer大小，必须是2的幂次方
        int bufferSize = 1024;
        
        // 创建线程工厂
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        
        // 1. 创建Disruptor实例
        Disruptor<LongEvent> disruptor = new Disruptor<>(
            new LongEventFactory(),           // 事件工厂
            bufferSize,                       // RingBuffer大小
            threadFactory,                    // 线程工厂
            ProducerType.SINGLE,              // 单生产者模式
            new BlockingWaitStrategy()        // 阻塞等待策略
        );
        
        // 2. 注册事件处理器（消费者）
        disruptor.handleEventsWith(new LongEventHandler());
        
        // 3. 启动Disruptor
        disruptor.start();
        
        // 4. 获取RingBuffer
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        
        // 5. 创建生产者
        Producer producer = new Producer(ringBuffer);
        
        // 6. 生产数据
        CountDownLatch latch = new CountDownLatch(5);
        for (long i = 0; i < 5; i++) {
            final long value = i;
            new Thread(() -> {
                producer.onData(value);
                System.out.println("Produced: " + value);
                latch.countDown();
            }).start();
            
            // 控制生产速度，便于观察
            Thread.sleep(100);
        }
        
        // 等待所有生产完成
        latch.await();
        
        // 7. 关闭Disruptor
        disruptor.shutdown();
        System.out.println("Disruptor shutdown completed");
    }
}
```



- 将disruptor封装成线程池的接口
- disruptor的动态参数设置 : 可以通过切换disruptor对象
  拒绝策略:统一封装的线程策略



### 线程池监控面板

不依赖第三方注册中心,实现统一的线程池管理



### starter

集成到boot应用

基于core:开发springboot组件,适配zk/nacos注册中心,实现通过注册中心动态刷新线程池



### 适配一些观测性组件

Prometheus

