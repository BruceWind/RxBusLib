/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.androidyuan.rxbus.component;

import android.util.SparseArray;

import com.androidyuan.rxbus.exception.BusException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriberMethodFinder {
    /*
     * In newer class files, compilers may add methods. Those are called bridge or synthetic
     * methods.
     * EventBus must ignore both. There modifiers are not public but defined in the Java class
     * file format:
     * http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.6-200-A.1
     */
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;

    private static final int MODIFIERS_IGNORE =
            Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    //由于反射中性能最弱的是 findMethod，不同的JDK版本都会比正常调用方法速度慢20倍以上， 所以这里做一个缓存，来提升性能
    private static final SparseArray<Method[]> METHOD_CACHE = new SparseArray<>();
    private static final int MAX_CACHE_SIZE = 100;

    static void clearCaches() {
        METHOD_CACHE.clear();
    }


    /**
     * @param subscriberCls
     * @return  一个可订阅的 method array
     */
    public static Method[] findSubscriberMethods(Class<?> subscriberCls) {

        Method[] methods;

        if (METHOD_CACHE.size() > MAX_CACHE_SIZE) {// auto clear cache
            clearCaches();
        }

        if (subscriberCls == null) {
            return new Method[0];
        }

        String clsName = subscriberCls.getName();

        if (METHOD_CACHE.indexOfKey(clsName.hashCode()) > -1) {
            return METHOD_CACHE.get(clsName.hashCode());
        } else {
            methods = getMethods(subscriberCls);
            if (methods == null) {
                methods = new Method[0];
            }
        }

        ArrayList<Method> methodList=new ArrayList<>();

        for (Method method:methods)
        {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {//判断修饰符
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {//判断参数 的个数
                    Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                    if (subscribeAnnotation != null) {
//                        Class<?> eventType = parameterTypes[0];
//                        String key=eventType.getName();
                        methodList.add(method);
                    }
                } else if (method.isAnnotationPresent(Subscribe.class)) {
                    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
//                    throw new BusException("@Subscribe method " + methodName +
//                            "must have exactly 1 parameter but has " + parameterTypes.length);
                }
            } else if (method.isAnnotationPresent(Subscribe.class)) {
                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
//                throw new BusException(methodName +
//                        " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
            }
        }

        return  methodList.toArray(new Method[0]);
    }


    public static Method[] getMethods(final Object subscriber) {

        if (subscriber == null) {
            return new Method[0];
        }

        Class<?> subscriberClass = subscriber.getClass();

        Method[] methods;
        try {
            // This is faster than getMethods, especially when subscribers are fat classes like
            // Activities
            return subscriberClass.getDeclaredMethods();
        } catch (Throwable th) {
            // Workaround for java.lang.NoClassDefFoundError, see https://github
            // .com/greenrobot/EventBus/issues/149
            return subscriberClass.getMethods();
        }
    }

}
