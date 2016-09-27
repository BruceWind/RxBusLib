package com.androidyuan.rxbus.component;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import com.androidyuan.rxbus.exception.REventIsNullException;

/**
 * Created by wei on 16/9/19.
 */
public class OnEvent {

    REvent mEvent;

    public OnEvent(REvent onEv) {

        mEvent = onEv;
    }

    /**
     * onEvent执行时所在的线程
     *
     * @return 发送的线程
     */
    protected ThreadMode getThreadMode() {

        return ThreadMode.BACKGROUND;
    }

    public Subscription event(Observable obs) {

        if (mEvent == null)//抛出异常
            throw new REventIsNullException();

        switch (getThreadMode()) {
            case BACKGROUND: {
                return obs.observeOn(Schedulers.newThread()).subscribe(mEvent);
            }
            case IO: {
                return obs.observeOn(Schedulers.io()).subscribe(mEvent);
            }
//            case MAIN: {
//                return obs.observeOn(AndroidSchedulers.mainThread()).subscribe(mEvent);
//            }
            case ASYNC: {
                return obs.observeOn(Schedulers.computation()).subscribe(mEvent);
            }
            case POSTING:
            default:
                return obs.subscribe(mEvent);
        }
    }


}
