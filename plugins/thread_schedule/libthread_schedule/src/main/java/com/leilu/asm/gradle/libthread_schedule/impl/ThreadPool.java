package com.leilu.asm.gradle.libthread_schedule.impl;


import com.leilu.asm.gradle.libthread_schedule.inter.IThreadPool;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 由于java library module引入android library后，并不能导入对应的注解类，所以这个模块
 * 被改成了java library，但这又带入了一个新的问题，就是不能使用安卓的Handler来发送主线程消息，
 * 所以这里为了方便测试，自己创建一个假的主线程来模拟安卓的主线程
 */
public class ThreadPool implements IThreadPool {

    private static final String THREAD_NAME_MAIN = "ThreadSchedulePlugin-MAIN";
    private static final String THREAD_NAME_BG = "ThreadSchedulePlugin-BG";

    private static final ScheduledExecutorService MAIN_THREAD_POOL = Executors.newScheduledThreadPool(0, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, THREAD_NAME_MAIN);
        }
    });

    private static final ScheduledExecutorService BG_THREAD_POOL = Executors.newScheduledThreadPool(0, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, THREAD_NAME_BG);
        }
    });


    @Override
    public void runOnBGThread(long delay, Runnable runnable) {
        if (delay > 0) {
            BG_THREAD_POOL.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        } else {
            BG_THREAD_POOL.execute(runnable);
        }
    }

    @Override
    public void runOnMainThread(long delay, Runnable runnable) {
        if (delay > 0) {
            MAIN_THREAD_POOL.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        } else {
            MAIN_THREAD_POOL.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean isMainThread() {
        return THREAD_NAME_MAIN.equals(Thread.currentThread().getName());
    }
}
