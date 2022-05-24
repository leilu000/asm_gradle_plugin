package com.leilu.asm.gradle.plugin;

import android.util.Log;

import com.leilu.asm.gradle.libthread_schedule.annotations.BGThread;
import com.leilu.asm.gradle.libthread_schedule.annotations.MainThread;

public class Test {


    @MainThread
    public void test() {
        // 当前运行线程应该是主线程
        Log.i("==", "无参无返回值的test方法，当前运行线程:" + Thread.currentThread().getName());
    }

    @BGThread(delay = 1000)
    public void test(String name, int age) {
        // 当前运行线程应该是非主线程，并且延迟1秒调用
        Log.i("==", "有参无返回值的test方法，当前运行线程:" + Thread.currentThread().getName()
                + "   参数，name:" + name + "  age:" + age);
    }

    @MainThread(delay = 5000)
    public String test(String name) {
        // 当前运行线程应该是主线程，并且延迟5秒调用
        Log.i("==", "有参有返回值的test方法，当前运行线程:" + Thread.currentThread().getName()
                + "   参数，name:" + name);
        return "有参有返回值的test方法返回的数据，参数name:" + name;
    }

    @BGThread
    public static void staticTest(int age) {
        // 当前运行线程应该是非主线程
        Log.i("==", "有参无返回值的静态方法 staticTest，当前运行线程:" + Thread.currentThread().getName()
                + "   参数，age:" + age);
    }

    @BGThread
    public static String staticTest(String name, int age) {
        // 当前运行线程应该是非主线程
        Log.i("==", "有参有返回值的静态方法 staticTest，当前运行线程:" + Thread.currentThread().getName()
                + "   参数，age:" + age);
        return "有参有返回值的test方法返回的数据，参数name:" + name;
    }

    public void testInnerClass() {
        new Thread(new Runnable() {
            // 由于这里使用了MainThread注解，所以run方法应该运行在主线程
            @MainThread
            @Override
            public void run() {
                Log.i("==", "匿名内部类调用，当前线程:" + Thread.currentThread().getName());
            }
        }).start();
    }
}
