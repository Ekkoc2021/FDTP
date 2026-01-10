package com.fdtp.dt.handler;

import com.fdtp.dt.FDTPoolConfig;
import com.fdtp.dt.FDynamicThreadPool;

public interface FDTPoolChangeHanlder {

    // 变动前置处理器
    void beforePoolChange(FDTPoolConfig config, FDynamicThreadPool dynamicThreadPool);

    // 变动后置处理
    void afterPoolChange(FDTPoolConfig config, FDynamicThreadPool dynamicThreadPool);
}
