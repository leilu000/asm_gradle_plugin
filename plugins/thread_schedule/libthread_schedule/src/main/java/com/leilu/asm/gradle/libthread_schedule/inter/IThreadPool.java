package com.leilu.asm.gradle.libthread_schedule.inter;

public interface IThreadPool {

    void runOnBGThread(long delay, Runnable runnable);

    void runOnMainThread(long delay, Runnable runnable);

    boolean isMainThread();
}
