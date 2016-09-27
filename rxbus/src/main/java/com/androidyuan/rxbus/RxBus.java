package com.androidyuan.rxbus;

import android.util.Log;
import android.util.SparseArray;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import com.androidyuan.rxbus.component.OnEvent;
import com.androidyuan.rxbus.component.Subscribe;
import com.androidyuan.rxbus.component.SubscriberMethod;
import  com.androidyuan.rxbus.component.SubscriberMethodFinder;
import com.androidyuan.rxbus.component.ThreadMode;
import com.androidyuan.rxbus.exception.BusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

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

    static RxBus instance;



    private final SubscriberMethodFinder subscriberMethodFinder;

    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;


    //use SparseArray,because is high performance
    SparseArray<List<Object>> mSparseArrOnEvent;

    RxBus(EventBusBuilder builder) {

        mSparseArrOnEvent = new SparseArray<>();
        subscriberMethodFinder = new SubscriberMethodFinder(builder.subscriberInfoIndexes,
                builder.strictMethodVerification);
    }


    public static RxBus getInstance() {

        if (instance == null) {
            instance = new EventBusBuilder().build();
        }
        return instance;
    }


    public void register(final Object subscriber) {

        if (subscriber==null)
            return;

        Method[] methods=getMethods(subscriber);

        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {//判断是否是pubulic
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {//判断参数 的个数
                    Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                    if (subscribeAnnotation != null) {
                        Class<?> eventType = parameterTypes[0];
                        String key=eventType.getName();
                        ThreadMode threadMode = subscribeAnnotation.threadMode();
                        putObject(key,subscriber);
                    }
                } else if (method.isAnnotationPresent(Subscribe.class)) {
                    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                    throw new BusException("@Subscribe method " + methodName +
                            "must have exactly 1 parameter but has " + parameterTypes.length);
                }
            } else if (method.isAnnotationPresent(Subscribe.class)) {
                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                throw new BusException(methodName +
                        " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
            }
        }

    }

    private Method[] getMethods(final Object subscriber)
    {

        if (subscriber==null)
            return null;

        Class<?> subscriberClass = subscriber.getClass();

        Method[] methods;
        try {
            // This is faster than getMethods, especially when subscribers are fat classes like Activities
            return subscriberClass.getDeclaredMethods();
        } catch (Throwable th) {
            // Workaround for java.lang.NoClassDefFoundError, see https://github.com/greenrobot/EventBus/issues/149
            return subscriberClass.getMethods();
        }
    }

    public void putObject(String key,Object object)
    {
        synchronized (mSparseArrOnEvent) {
            List<Object> handList = new ArrayList<>();
            if (mSparseArrOnEvent.indexOfKey(key.hashCode()) > -1) {
                handList = mSparseArrOnEvent.get(key.hashCode());
            }
            else
            {
                mSparseArrOnEvent.put(key.hashCode(),handList);
            }


            if (!handList.contains(object)) {
                handList.add(object);
            }
        }
    }

    public void removeObject(Object object)
    {
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
     *
     * @param obj
     */
    public void unRegister(Object obj) {

        if (obj==null)
            return;


    }



    /**
     * 不带线程切换 功能
     * action的触发会在发送的observable所在线程线程执行
     *
     * @param event
     */
    public void post(final Object event) {

        if ( event == null)
            return;
        String filter=event.getClass().getName();

        if (mSparseArrOnEvent.indexOfKey(filter.hashCode()) > -1) {//设计的 就像  广播一样 发送出来 如果没有人接受 就丢弃了

            List<Object> handQueue=mSparseArrOnEvent.get(filter.hashCode());

            for(Object hand:handQueue)
            {
                Method[] methods=getMethods(hand);

                for (Method method : methods) {
                    int modifiers = method.getModifiers();
                    if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {//判断是否是pubulic
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length == 1) {//判断参数 的个数
                            Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                            if (subscribeAnnotation != null) {
                                ThreadMode threadMode = subscribeAnnotation.threadMode();
                                invokeSubscriber(hand,method,threadMode,event);
                            }
                        }
                    }
                }
            }





        }
    }


    /**
     * 反射 调用hand对象的 method方法 把event传递进去，以threadMode作为线程切换的依据
     * @param hand
     * @param method
     * @param threadMode
     * @param event
     */
    void invokeSubscriber(Object hand,Method method,ThreadMode threadMode, Object event) {
        try {
            method.invoke(hand, event);
        } catch (InvocationTargetException e) {

        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }


}