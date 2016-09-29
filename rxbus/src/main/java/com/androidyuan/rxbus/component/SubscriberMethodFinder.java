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

import android.util.Log;
import android.util.SparseArray;

import com.androidyuan.rxbus.exception.BusException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    //由于反射中性能最弱的是 findMethod，不同的JDK版本都会比正常调用方法速度慢20倍以上， 所以这里做一个缓存，来提升性能
    private static final SparseArray<Method[]> METHOD_CACHE = new SparseArray<>();
    private static final int MAX_CACHE_SIZE = 100;

    static void clearCaches() {
        METHOD_CACHE.clear();
    }


    /**
     * @param subscriber
     * @return  一个可订阅的 method array
     */
    public Method[] findSubscriberMethods(final Object subscriber) {

        if (subscriber == null) {
            return new Method[0];
        }

        if(containKey(subscriber))
        {
            return METHOD_CACHE.get(subscriber.getClass().getName().hashCode());
        }


        Class<?> subscriberClass = subscriber.getClass();

        Method[] methods;
        try {
            // This is faster than getMethods, especially when subscribers are fat classes like
            // Activities
            methods =  subscriberClass.getDeclaredMethods();
        } catch (Throwable th) {
            // Workaround for java.lang.NoClassDefFoundError, see https://github
            // .com/greenrobot/EventBus/issues/149
            methods = subscriberClass.getMethods();
        }

        if(METHOD_CACHE.size()>MAX_CACHE_SIZE) {
            clearCaches();
        }

        List<Method> methodList=new ArrayList<>();
        for(Method method:methods)
        {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {//判断是否是pubulic
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {//判断参数 的个数
                    Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                    if (subscribeAnnotation != null) {
                        methodList.add(method);
                        Log.d("Method",""+method.getName());
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

        methods=new Method[methodList.size()];
        methodList.toArray(methods);

        METHOD_CACHE.put(subscriber.getClass().getName().hashCode(),methods);
        return methods;
    }

    private boolean containKey(Object obj)
    {
        if(obj==null)
            return false;
        return METHOD_CACHE.indexOfKey(obj.getClass().getName().hashCode())>-1;
    }

}
