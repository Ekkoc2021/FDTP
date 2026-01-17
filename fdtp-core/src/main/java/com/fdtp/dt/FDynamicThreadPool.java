package com.fdtp.dt;


import com.fdtp.dt.handler.FDTPoolRejectedHandler;
import com.fdtp.dt.handler.FDTPoolChangeHanlder;
import com.fdtp.dt.pipe.FDTPChangePipeline;
import com.fdtp.dt.pipe.FDTPRejectedPipeline;


import java.util.concurrent.RejectedExecutionHandler;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * 动态线程池接口
 */
public abstract class FDynamicThreadPool  {

    // 可配置改变前和改变后的处理器
    private FDTPChangePipeline changePipeline = new FDTPChangePipeline();

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

        RejectedExecutionHandler rh=new RejectedExecutionHandler(){
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                rejectedPipeline.process(r,fdtp);
                rejectedExecutionHandler.rejectedExecution(r,executor);
            }
        };

        this.rejectedExecutionHandler = rh;   // 创建代理类
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
