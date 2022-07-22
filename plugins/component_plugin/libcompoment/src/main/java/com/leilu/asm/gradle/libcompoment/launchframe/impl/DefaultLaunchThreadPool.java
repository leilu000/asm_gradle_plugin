package com.leilu.asm.gradle.libcompoment.launchframe.impl;


import android.os.Handler;
import android.os.Looper;

import com.leilu.asm.gradle.libcompoment.launchframe.base.IThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DefaultLaunchThreadPool implements IThreadPool {

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mIoThreadPoll = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "IO-THREAD");
        }
    });


    @Override
    public boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    @Override
    public void executeOnMainThread(Runnable runnable) {
        mMainThreadHandler.post(runnable);
    }

    @Override
    public void executeOnIOThread(Runnable runnable) {
        mIoThreadPoll.execute(runnable);
    }

    @Override
    public void stopAll() {
        mMainThreadHandler.removeCallbacksAndMessages(null);
        mIoThreadPoll.shutdownNow();
    }
}
