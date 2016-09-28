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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Used internally by RxBus and generated subscriber indexes. */
public class RxSubscriberMethod {

    final Method method;
    final ThreadMode threadMode;
    final Object event;
    final Object hand;
    public RxSubscriberMethod(Object hand,Method method, Object event, ThreadMode threadMode) {
        this.method = method;
        this.threadMode = threadMode;
        this.event = event;
        this.hand=hand;
    }


    @Override
    public int hashCode() {
        return method.hashCode();
    }

    public String getEvent() {
        return event+"";
    }


    /**
     * 反射 调用hand对象的 method方法 把event传递进去，以threadMode作为线程切换的依据
     */
    public void invokeSubscriber() {
        try {
            method.invoke(hand, event);
        } catch (InvocationTargetException e) {

        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }
}