package com.leilu.asm.gradle.libthread_schedule;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TryCatchCountDownLatch extends CountDownLatch {

    public TryCatchCountDownLatch() {
        super(1);
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {
        try {
            return super.await(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void await() {
        try {
            super.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
