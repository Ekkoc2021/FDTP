package com.fdtp.dt.pipe;

import com.fdtp.dt.FDynamicThreadPool;
import com.fdtp.dt.handler.FDTPoolRejectedHandler;

import java.util.LinkedList;
import java.util.ListIterator;

public class FDTPRejectedPipeline {
    private LinkedList<FDTPoolRejectedHandler> chains = new LinkedList<>();

    public void process(Runnable r, FDynamicThreadPool pool){
        ListIterator<FDTPoolRejectedHandler> fDTPoolRejectedHandlerListIterator = chains.listIterator();
        while(fDTPoolRejectedHandlerListIterator.hasNext()){
            fDTPoolRejectedHandlerListIterator.next().process(r, pool);
        }
    }

    public LinkedList<FDTPoolRejectedHandler> getChains() {
        return chains;
    }

    public void setChains(LinkedList<FDTPoolRejectedHandler> chains) {
        this.chains = chains;
    }
}
