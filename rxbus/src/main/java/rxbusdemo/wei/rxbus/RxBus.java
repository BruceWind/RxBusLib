package rxbusdemo.wei.rxbus;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rxbusdemo.wei.rxbus.component.OnEvent;

/**
 * Created by wei on 16-9-10.
 *
 * just like EventBus method
 1、onEvent
 2、onEventMainThread
 3、onEventBackgroundThread
 4、onEventAsync

 EventBus 不同参数类型的方法 来实现 onEvent
 目前没有完全模仿 EventBus 目前是使用 OnEvent类来实现 这样子更加自由 不需要为了各种类型建立各种类
 */
public class RxBus {

    private static RxBus instance;

    //use SparseArray,because is high performance
    SparseArray<OnEvent> mSparseArrOnEvent;
    CompositeSubscription mCompositeSubscription;

    private RxBus() {

        mSparseArrOnEvent = new SparseArray<>();
        mCompositeSubscription = new CompositeSubscription();
    }


    public static RxBus getInstance() {

        if (instance == null) {
            instance = new RxBus();
        }
        return instance;
    }


    public void register(String filter, OnEvent event) {

        if (!TextUtils.isEmpty(filter) && event != null) {
            mSparseArrOnEvent.put(filter.hashCode(), event);
        }
    }

    /**
     * 解绑
     *
     * @param filter
     */
    public void unRegister(String filter) {

        if (TextUtils.isEmpty(filter))
            return;

        if (mSparseArrOnEvent.indexOfKey(filter.hashCode()) > -1) {
            mSparseArrOnEvent.remove(filter.hashCode());
        }
    }

    /**
     * 解绑
     *
     * @param onEv
     */
    public void unRegister(OnEvent onEv) {

        if (onEv == null)
            return;

        int index = mSparseArrOnEvent.indexOfValue(onEv);
        if (index > -1) {
            mSparseArrOnEvent.remove(index);
        }
    }


    /**
     * 不带线程切换 功能
     * action的触发会在发送的observable所在线程线程执行
     *
     * @param filter
     * @param obj
     */
    public void post(String filter, Object obj) {

        if (TextUtils.isEmpty(filter) || obj == null)
            return;

        if (mSparseArrOnEvent.indexOfKey(filter.hashCode()) > -1) {//设计的 就像  广播一样 发送出来 如果没有人接受 就丢弃了

            OnEvent onEv = mSparseArrOnEvent.get(filter.hashCode());
            Subscription sbus = onEv.event(Observable.just(obj));

            Log.d("on send ,isUnsubscribed", sbus.isUnsubscribed() + "");
        }
    }


}
