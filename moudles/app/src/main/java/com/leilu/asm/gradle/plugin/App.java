package com.leilu.asm.gradle.plugin;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.leilu.asm.gradle.libcompoment.launchframe.LaunchTaskManager;
import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTaskListener;
import com.leilu.asm.gradle.libthread_schedule.ThreadScheduleUtil;
import com.leilu.asm.gradle.libthread_schedule.inter.IThreadPool;
import com.leilu.asm.gradle.plugin.launch.A0;
import com.leilu.asm.gradle.plugin.launch.B1;
import com.leilu.asm.gradle.plugin.launch.C2;
import com.leilu.asm.gradle.plugin.launch.D3;
import com.leilu.asm.gradle.plugin.launch.E4;
import com.leilu.asm.gradle.plugin.launch.F5;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class App extends Application {

    private static final ScheduledExecutorService BG_THREAD_POOL
            = Executors.newScheduledThreadPool(0, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "BG-Thread");
        }
    });

    private static final Handler mMainHandler = new Handler(Looper.getMainLooper());


    @Override
    public void onCreate() {
        super.onCreate();
        ThreadScheduleUtil.getInstance().setThreadPool(new IThreadPool() {
            @Override
            public void runOnBGThread(long delay, Runnable runnable) {
                BG_THREAD_POOL.schedule(runnable, delay, TimeUnit.MILLISECONDS);
            }

            @Override
            public void runOnMainThread(long delay, Runnable runnable) {
                mMainHandler.postDelayed(runnable, delay);
            }

            @Override
            public boolean isMainThread() {
                return Looper.getMainLooper() == Looper.myLooper();
            }
        });

        new LaunchTaskManager()
                .addTask(A0.class)
                .addTask(B1.class)
                .addTask(C2.class)
                .addTask(D3.class)
                .addTask(E4.class)
                .addTask(F5.class)
                .addLaunchTaskListener(new ILaunchTaskListener() {
                    @Override
                    public void onTaskStarted(String taskName) {
                        System.out.println("onTaskStarted:" + taskName);
                    }

                    @Override
                    public void onTaskCompleted(String taskName) {
                        System.out.println("onTaskCompleted:" + taskName);
                    }

                    @Override
                    public void onAllTaskCompleted() {
                        System.out.println("onAllTaskCompleted");
                    }
                })
                .start()
                .awaitAllTaskComplete();
        System.out.println("============");
    }
}
