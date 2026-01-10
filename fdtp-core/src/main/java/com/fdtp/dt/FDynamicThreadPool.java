package com.fdtp.dt;


import com.fdtp.dt.handler.FDTPoolRejectedHandler;
import com.fdtp.dt.handler.FDTPoolChangeHanlder;
import com.fdtp.dt.pipe.FDTPChangePipeline;
import com.fdtp.dt.pipe.FDTPRejectedPipeline;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 动态线程池接口
 */
public abstract class FDynamicThreadPool extends ThreadPoolExecutor {

    // 可配置改变前和改变后的处理器
    private FDTPChangePipeline changePipeline = new FDTPChangePipeline();

    public FDynamicThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public FDynamicThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public FDynamicThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public FDynamicThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public void addChangeHandler(FDTPoolChangeHanlder hanlder) {
        changePipeline.getChains().addLast(hanlder);
    }

    // 可配置拒绝处理器
    private FDTPRejectedPipeline rejectedPipeline = new FDTPRejectedPipeline();

    public void addRejectedHandler(FDTPoolRejectedHandler rejectedHandler) {
        rejectedPipeline.getChains().addLast(rejectedHandler);
    }

    // RejectedExecutionHandler对象不允许有状态，真实的RejectedExecutionHandler走了一层代理。
    protected RejectedExecutionHandler rejectedExecutionHandler;

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    // 拒绝策略动态代理
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {

        FDynamicThreadPool fdtp = this;
        // 动态代理
        MethodInterceptor proxy = new MethodInterceptor() {
            /**
             * @param object 表示要进行增强的对象
             * @param method 表示拦截的方法
             * @param args 数组表示参数列表
             * @param methodProxy 表示对方法的代理，invokeSuper方法表示对被代理对象方法的调用
             * @return 执行结果
             * @throws Throwable
             */
            @Override
            public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                // 走pipeline处理的时候传入 FDynamicThreadPool: Runnable r, FDynamicThreadPool pool
                rejectedPipeline.process((Runnable) args[0], fdtp);

                // 走真实拒绝策略的时候 转用ThreadPoolExcutor
                Object result = methodProxy.invokeSuper(object, args);
                return result;
            }
        };

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(RejectedExecutionHandler.class);
        enhancer.setCallback(proxy);
        this.rejectedExecutionHandler = (RejectedExecutionHandler) enhancer.create();   // 创建代理类
    }


    public boolean changes(FDTPoolConfig config) {

        boolean isChanges = false;

        if (changePipeline != null) {
            changePipeline.beforeChanges(config, this);
        }

        isChanges = update(config);

        if (changePipeline != null) {
            changePipeline.afterChanges(config, this);
        }

        return isChanges;
    }

    /**
     * 具体实现的类进行实现
     *
     * @param config
     * @return
     */
    protected abstract boolean update(FDTPoolConfig config);


}
