package com.androidyuan.rxbus.component;

/**
 * Created by wei on 16/9/19.
 */
public class OnEventMainThread extends OnEvent {

    public OnEventMainThread(REvent onEv) {

        super(onEv);
    }

    @Override
    protected ThreadMode getThreadMode() {

        return ThreadMode.MAIN;
    }
}
