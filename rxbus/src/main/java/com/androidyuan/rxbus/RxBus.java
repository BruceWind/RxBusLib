package com.androidyuan.rxbus;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import com.androidyuan.rxbus.component.OnEvent;
import com.androidyuan.rxbus.component.RxSubscriberMethod;
import com.androidyuan.rxbus.component.Subscribe;
import com.androidyuan.rxbus.component.SubscriberMethodFinder;
import com.androidyuan.rxbus.component.ThreadMode;
import com.androidyuan.rxbus.exception.BusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wei on 16-9-10.
 *
 *
 * EventBus 不同参数类型的方法 来实现 onEvent
 * 目前没有完全模仿 EventBus 目前是使用 OnEvent类来实现 这样子更加自由 不需要为了各种类型建立各种类
 */
public class RxBus {

    static RxBus instance;

    private final SubscriberMethodFinder subscriberMethodFinder;

    //use SparseArray,because is high performance
    SparseArray<List<Object>> mSparseArrOnEvent;

    RxBus() {

        mSparseArrOnEvent = new SparseArray<>();
        subscriberMethodFinder = new SubscriberMethodFinder();
    }


    public static RxBus getInstance() {

        if (instance == null) {
            instance = new RxBus();
        }
        return instance;
    }

    public void register(final Object subscriber) {

        if (subscriber == null) {
            return;
        }

        Observable.just(subscriber)
                .concatMap(new Func1<Object, Observable<Method>>() {
                    @Override
                    public Observable<Method> call(Object subs) {

                        Method[] methods = subscriberMethodFinder.findSubscriberMethods(subs);

                        return Observable.from(methods);
                    }
                })
                .subscribe(new Action1<Method>() {
                    @Override
                    public void call(Method method) {

                        Class<?>[] parameterTypes = method.getParameterTypes();
                        //判断参数 的个数
                        Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                        if (subscribeAnnotation != null) {
                            Class<?> eventType = parameterTypes[0];
                            String key = eventType.getName();
                            ThreadMode threadMode = subscribeAnnotation.threadMode();
                            putObject(key, subscriber);
                        }
                    }
                });

    }


    public void putObject(String key, Object object) {
        synchronized (mSparseArrOnEvent) {
            List<Object> handList = new ArrayList<>();
            if (mSparseArrOnEvent.indexOfKey(key.hashCode()) > -1) {
                handList = mSparseArrOnEvent.get(key.hashCode());
            } else {
                mSparseArrOnEvent.put(key.hashCode(), handList);
            }

            if (!handList.contains(object)) {
                handList.add(object);
            }
        }
    }

    public void removeObject(Object object) {
        synchronized (mSparseArrOnEvent) {

            int len = mSparseArrOnEvent.size();

            for (int index = 0; index < len; index++) {
                List<Object> list = mSparseArrOnEvent.get(mSparseArrOnEvent.keyAt(index));
                if (list.contains(object)) {
                    list.remove(object);
                }
            }
        }
    }

    /**
     * 解绑
     */
    public void unRegister(Object obj) {

        if (obj == null) {
            return;
        }

        removeObject(obj);
    }

    /**
     * 不带线程切换 功能
     * action的触发会在发送的observable所在线程线程执行
     * 这里使用lambda更佳，但是作为一个 lib ，不应该使用 lambda
     */
    public void post(final Object event) {

        if (event == null) {
            return;
        }
        String filter = event.getClass().getName();

        Observable.just(filter)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.immediate())
                .concatMap(new Func1<String, Observable<Object>>() {
                    @Override
                    public Observable<Object> call(String f) {
                        if (containKey(f)) {
                            Object[] array = new Object[mSparseArrOnEvent.get(f.hashCode()).size()];
                            mSparseArrOnEvent.get(f.hashCode()).toArray(array); // fill the array
                            return Observable.from(mSparseArrOnEvent.get(f.hashCode()));
                        } else {
                            return Observable.from(new Object[0]);// if return null,will crash.
                        }
                    }
                })
                .concatMap(new Func1<Object, Observable<RxSubscriberMethod>>() {

                    @Override
                    public Observable<RxSubscriberMethod> call(Object hand) {
                        List<RxSubscriberMethod> listSubs = new ArrayList<>();
                        if (hand == null) {
                            return Observable.from(listSubs);
                        }

                        Method[] methods = subscriberMethodFinder.findSubscriberMethods(hand);

                        for (Method method : methods) {
                            Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                            if (subscribeAnnotation != null) {
                                ThreadMode threadMode = subscribeAnnotation.threadMode();
                                listSubs.add(
                                        new RxSubscriberMethod(hand, method, event, threadMode));
                            }
                        }
                        return Observable.from(listSubs);
                    }
                })
                .subscribe(new Action1<RxSubscriberMethod>() {
                    @Override
                    public void call(RxSubscriberMethod rxSubscriberMethod) {
                        new OnEvent(rxSubscriberMethod).event();
                    }
                });

    }

    private boolean containKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        } else {
            return mSparseArrOnEvent.indexOfKey(key.hashCode()) > -1;
        }
    }
}
