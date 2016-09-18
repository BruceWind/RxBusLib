package rxbusdemo.wei.rxbus.component;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rxbusdemo.wei.rxbus.exception.REventIsNullException;

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
    protected int getThreadMode() {

        return BusThreadModel.THREAD_IMMEDIATE;
    }

    public Subscription event(Observable obs) {

        if (mEvent == null)//抛出异常
            throw new REventIsNullException();

        switch (getThreadMode()) {
            case BusThreadModel.THREAD_ASYNC: {
                return obs.observeOn(Schedulers.newThread()).subscribe(mEvent);
            }
            case BusThreadModel.THREAD_IO: {
                return obs.observeOn(Schedulers.io()).subscribe(mEvent);
            }
            case BusThreadModel.THREAD_MAINTHREAD: {
                return obs.observeOn(AndroidSchedulers.mainThread()).subscribe(mEvent);
            }
            case BusThreadModel.THREAD_COMPUTION: {
                return obs.observeOn(Schedulers.computation()).subscribe(mEvent);
            }
            default:
                return obs.subscribe(mEvent);
        }
    }


}
