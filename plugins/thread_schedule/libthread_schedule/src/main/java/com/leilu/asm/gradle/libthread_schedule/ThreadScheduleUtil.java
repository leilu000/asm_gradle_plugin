package com.leilu.asm.gradle.libthread_schedule;

import android.os.Looper;

import com.leilu.asm.gradle.libthread_schedule.impl.ThreadPool;
import com.leilu.asm.gradle.libthread_schedule.inter.IThreadPool;

public class ThreadScheduleUtil implements IThreadPool {

    private static volatile ThreadScheduleUtil sInstance;
    private IThreadPool mThreadPool;

    private ThreadScheduleUtil() {
        mThreadPool = new ThreadPool();
    }

    public static ThreadScheduleUtil getInstance() {
        if (sInstance == null) {
            synchronized (ThreadScheduleUtil.class) {
                if (sInstance == null) {
                    sInstance = new ThreadScheduleUtil();
                }
            }
        }
        return sInstance;
    }

    public void setThreadPool(IThreadPool threadPool) {
        if (threadPool != null) {
            mThreadPool = threadPool;
        }
    }

    @Override
    public void runOnBGThread(long delay, Runnable runnable) {
        if (Looper.getMainLooper() != Looper.myLooper() && delay <= 0) {
            runnable.run();
            return;
        }
        mThreadPool.runOnBGThread(delay, runnable);
    }

    @Override
    public void runOnMainThread(long delay, Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper() && delay <= 0) {
            runnable.run();
            return;
        }
        mThreadPool.runOnMainThread(delay, runnable);
    }
}
