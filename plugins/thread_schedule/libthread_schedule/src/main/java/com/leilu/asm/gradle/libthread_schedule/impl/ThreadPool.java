package com.leilu.asm.gradle.libthread_schedule.impl;

import android.os.Handler;
import android.os.Looper;

import com.leilu.asm.gradle.libthread_schedule.inter.IThreadPool;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool implements IThreadPool {

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadSchedulePlugin-BG");
        }
    });


    @Override
    public void runOnBGThread(long delay, Runnable runnable) {
        if (delay > 0) {
            scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        } else {
            scheduler.execute(runnable);
        }
    }

    @Override
    public void runOnMainThread(long delay, Runnable runnable) {
        if (delay > 0) {
            handler.postDelayed(runnable, delay);
        } else {
            handler.post(runnable);
        }
    }
}
