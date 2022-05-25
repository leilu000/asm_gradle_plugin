package com.leilu.asm.gradle.libthread_schedule;


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
        mThreadPool.runOnBGThread(delay, runnable);
    }

    @Override
    public void runOnMainThread(long delay, Runnable runnable) {
        mThreadPool.runOnMainThread(delay, runnable);
    }

    @Override
    public boolean isMainThread() {
        return mThreadPool.isMainThread();
    }
}
