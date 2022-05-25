package com.leilu.asm.gradle.testjava;

import com.leilu.asm.gradle.libthread_schedule.annotations.MainThread;

public class MyClass {

    private String name;

    public MyClass(String name) {
        this.name = name;
    }
    public void invokeTestMethod(MyClass myClass) {
        test(myClass);
    }

    @MainThread
    private void test(MyClass myClass) {
        System.out.println("MyClass类的test方法，name:" + myClass + "  " + "  当前线程:" + Thread.currentThread().getName());
    }
}