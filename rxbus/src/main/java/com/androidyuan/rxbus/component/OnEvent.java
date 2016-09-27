package com.androidyuan.rxbus.component;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import com.androidyuan.rxbus.exception.REventIsNullException;

/**
 * Created by wei on 16/9/19.
 * 这个类主要用于线程切换
 */
public class OnEvent {

    RxSubscriberMethod mRxSubscriberMethod;

    public OnEvent(RxSubscriberMethod onEv) {

        mRxSubscriberMethod = onEv;
    }

    /**
     * onEvent执行时所在的线程
     *
     * @return 发送的线程
     */
    protected ThreadMode getThreadMode() {

        return mRxSubscriberMethod.threadMode;
    }

    public Subscription event() {
        Observable obs=Observable.just(mRxSubscriberMethod.event);

        if (mRxSubscriberMethod == null)//抛出异常
            throw new REventIsNullException();

        REvent rEvent=new REvent() {
            @Override
            public void call(Object o) {
                mRxSubscriberMethod.invokeSubscriber();
            }
        };

        switch (getThreadMode()) {
            case BACKGROUND: {
                return obs.subscribeOn(Schedulers.newThread()).subscribe(rEvent);
            }
            case IO: {
                return obs.subscribeOn(Schedulers.io()).subscribe(rEvent);
            }
            case MAIN: {
                return obs.subscribeOn(AndroidSchedulers.mainThread()).subscribe(rEvent);
            }
            case ASYNC: {
                return obs.subscribeOn(Schedulers.computation()).subscribe(rEvent);
            }
            case POSTING:
            default:
                return obs.subscribeOn(Schedulers.immediate()).subscribe(rEvent);
        }
    }


}
