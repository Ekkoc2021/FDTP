package com.fdtp.dt.handler;

import com.fdtp.dt.FDynamicThreadPool;

public interface FDTPoolRejectedHandler {
    /**
     * Object[] args :　
     * @param r
     * @param pool
     */
    void process(Runnable r, FDynamicThreadPool pool);
}
