package com.fdtp.dt.pipe;

import com.fdtp.dt.FDTPoolConfig;
import com.fdtp.dt.FDynamicThreadPool;
import com.fdtp.dt.handler.FDTPoolChangeHanlder;

import java.util.LinkedList;
import java.util.ListIterator;

public class FDTPChangePipeline {

    private LinkedList<FDTPoolChangeHanlder> chains = new LinkedList<>();

    public void afterChanges(FDTPoolConfig config, FDynamicThreadPool dynamicThreadPool) {
        ListIterator<FDTPoolChangeHanlder> fdtPoolChangeHanlderListIterator = chains.listIterator(chains.size());
        while (fdtPoolChangeHanlderListIterator.hasPrevious()) {
            fdtPoolChangeHanlderListIterator.previous().afterPoolChange(config, dynamicThreadPool);
        }
    }

    public void beforeChanges(FDTPoolConfig config, FDynamicThreadPool dynamicThreadPool) {
        ListIterator<FDTPoolChangeHanlder> fdtPoolChangeHanlderListIterator = chains.listIterator();
        while (fdtPoolChangeHanlderListIterator.hasNext()) {
            fdtPoolChangeHanlderListIterator.next().beforePoolChange(config, dynamicThreadPool);
        }
    }

    public LinkedList<FDTPoolChangeHanlder> getChains() {
        return chains;
    }

    public void setChains(LinkedList<FDTPoolChangeHanlder> chains) {
        this.chains = chains;
    }
}
