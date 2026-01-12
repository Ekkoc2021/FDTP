package com.fdtp.dt.tp;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class DisrutorThreadPoolExecutor implements ExecutorService {


    public DisrutorThreadPoolExecutor(int corePoolSize) {
        Worker[] workers = new Worker[corePoolSize];
        for (int i = 0; i < corePoolSize; i++) {
            workers[i] = new Worker();
        }
        this.disruptor.handleEventsWithWorkerPool(workers);

        disruptor.start();
    }


    // 事件
    private class TaskEvent {
        private Runnable task;

        public Runnable getTask() {
            return task;
        }

        public void setTask(Runnable task) {
            this.task = task;
        }
    }

    private class Worker implements WorkHandler<TaskEvent> {
        @Override
        public void onEvent(TaskEvent event) throws Exception {
            event.getTask().run();
        }
    }

    NamedThreadFactory threadFactory = new NamedThreadFactory("testPool", "testThread", false);

    // disruptor对象
    private Disruptor<TaskEvent> disruptor = new Disruptor<>(
            TaskEvent::new,
            524288,
            threadFactory,
            ProducerType.MULTI,
            new SleepingWaitStrategy() // 平衡策略
    );

    private class NamedThreadFactory implements ThreadFactory {
        private String poolName;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final boolean daemon;
        private String threadPrefix;

        public NamedThreadFactory(String poolName, String threadPrefix, boolean daemon) {
            this.poolName = poolName;
            this.daemon = daemon;
            this.threadPrefix = threadPrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r,
                    String.format("%s-%s-%d", poolName, threadPrefix, threadNumber.getAndIncrement()));
            thread.setDaemon(daemon);
            thread.setPriority(Thread.NORM_PRIORITY);

            return thread;
        }
    }

    @Override
    public void shutdown() {
        disruptor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return List.of();
    }

    @Override
    public boolean isShutdown() {
        RingBuffer<?> ringBuffer = disruptor.getRingBuffer();
        return ringBuffer.getCursor() == -1;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }


    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return null;
    }


    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return List.of();
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return List.of();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public void execute(Runnable command) {
        RingBuffer<TaskEvent> ringBuffer = disruptor.getRingBuffer();
        try {
            long next = ringBuffer.tryNext();
            TaskEvent taskEvent = ringBuffer.get(next);
            taskEvent.setTask(command);
            ringBuffer.publish(next);
        } catch (InsufficientCapacityException e) {
            // todo : 拒绝策略
            throw new RuntimeException(e);
        }
    }
}
