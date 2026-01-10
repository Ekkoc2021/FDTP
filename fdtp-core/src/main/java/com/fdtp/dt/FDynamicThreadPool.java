package com.fdtp.dt;


import com.fdtp.dt.handler.FDTPoolRejectedHandler;
import com.fdtp.dt.handler.FDTPoolChangeHanlder;
import com.fdtp.dt.pipe.FDTPChangePipeline;
import com.fdtp.dt.pipe.FDTPRejectedPipeline;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * 动态线程池接口
 */
public abstract class FDynamicThreadPool {



    // 可配置改变前和改变后的处理器
    private FDTPChangePipeline changePipeline = new FDTPChangePipeline();

    public void addChangeHandler(FDTPoolChangeHanlder hanlder){
        changePipeline.getChains().addLast(hanlder);
    }

    // 可配置拒绝处理器
    private FDTPRejectedPipeline rejectedPipeline = new FDTPRejectedPipeline();

    public  void addRejectedHandler(FDTPoolRejectedHandler rejectedHandler){
        rejectedPipeline.getChains().addLast(rejectedHandler);
    }

    protected RejectedExecutionHandler rejectedExecutionHandler;

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    // 拒绝策略动态代理
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {

        // todo: 这里有问题! 拒绝策略是普通JDK的线程池的拒绝策略!! 拒绝策略的执行入参就是普通JDK的入参
        // 动态代理
        MethodInterceptor proxy = new MethodInterceptor(){
            /**
             * @param object 表示要进行增强的对象
             * @param method 表示拦截的方法
             * @param args 数组表示参数列表，基本数据类型需要传入其包装类型，如int-->Integer、long-Long、double-->Double
             * @param methodProxy 表示对方法的代理，invokeSuper方法表示对被代理对象方法的调用
             * @return 执行结果
             * @throws Throwable
             */
            @Override
            public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                rejectedPipeline.process(args);
                Object result = methodProxy.invokeSuper(object, args);
                return result;
            }
        };


    }


    public boolean changes(FDTPoolConfig config) {

        boolean isChanges=false;

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
     * @param config
     * @return
     */
    protected abstract boolean update(FDTPoolConfig config);


}
