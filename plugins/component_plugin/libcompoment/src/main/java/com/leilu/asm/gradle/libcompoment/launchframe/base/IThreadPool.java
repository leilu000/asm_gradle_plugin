package com.leilu.asm.gradle.libcompoment.launchframe.base;

public interface IThreadPool {

    boolean isMainThread();

    void executeOnMainThread(Runnable runnable);

    void executeOnIOThread(Runnable runnable);

    void stopAll();
}
