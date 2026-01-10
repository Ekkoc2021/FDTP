package com.fdtp.dt.pipe;

import com.fdtp.dt.FDynamicThreadPool;
import com.fdtp.dt.handler.FDTPoolRejectedHandler;

import java.util.LinkedList;
import java.util.ListIterator;

public class FDTPRejectedPipeline {
    private LinkedList<FDTPoolRejectedHandler> chains = new LinkedList<>();

    public void process(Object[] args){
        ListIterator<FDTPoolRejectedHandler> fDTPoolRejectedHandlerListIterator = chains.listIterator();
        while(fDTPoolRejectedHandlerListIterator.hasNext()){
            if (args!=null && args.length>2){
//                Runnable r, FDynamicThreadPool pool
                Runnable r=(Runnable) args[0];
                FDynamicThreadPool pool=(FDynamicThreadPool) args[1];
                fDTPoolRejectedHandlerListIterator.next().process(r, pool);
            }

        }
    }

    public LinkedList<FDTPoolRejectedHandler> getChains() {
        return chains;
    }

    public void setChains(LinkedList<FDTPoolRejectedHandler> chains) {
        this.chains = chains;
    }
}
