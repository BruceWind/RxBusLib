package com.androidyuan.rxbus.component;

/**
 * Created by wei on 16/9/19.
 */
public class OnEventAsync extends OnEvent {

    public OnEventAsync(REvent onEv) {

        super(onEv);
    }

    @Override
    protected int getThreadMode() {

        return BusThreadModel.THREAD_ASYNC;
    }
}
