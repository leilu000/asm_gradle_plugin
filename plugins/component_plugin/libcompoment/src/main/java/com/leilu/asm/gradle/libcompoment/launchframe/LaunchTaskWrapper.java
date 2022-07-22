package com.leilu.asm.gradle.libcompoment.launchframe;


import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTask;
import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTaskListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * 包装了ILaunchTask任务，主要做以下事情：
 * 1、保存本任务的所有依赖的LaunchTaskWrapper
 * 2、保存所有需要依赖本任务的task的CountDownLatch
 * 3、在执行run方法的时候先调用本任务的CountDownLatch的await()方法等待本任务依赖的其他任务执行完成
 * 4、运行完成以后调用需要依赖本任务的其他任务的CountDownLatch的countdown方法
 */
public class LaunchTaskWrapper implements Runnable {

    // 本任务
    private final ILaunchTask mLaunchTask;

    // 保存被依赖的任务的CountDownLatch，在本人无执行完成以后调用被依赖任务的countDown方法
    private final List<CountDownLatch> mByDependencyCountDownLatchList = new LinkedList<>();

    // 保存本任务需要依赖的LaunchTaskWrapper，这里主要用来做有向无环图排序的时候确定任务的入度数
    private final List<LaunchTaskWrapper> mDependencyWrapperList = new CopyOnWriteArrayList<>();

    // 本任务的CountDownLatch，根据依赖的任务的个数来实例化，如果不依赖任何任务则为空
    private CountDownLatch mCountDownLatch;

    private final ILaunchTaskListener mLaunchTaskListener;

    public LaunchTaskWrapper(ILaunchTask task, ILaunchTaskListener launchTaskListener) {
        mLaunchTask = task;
        mLaunchTaskListener = launchTaskListener;
        if (task.dependencyTasks() != null && task.dependencyTasks().size() != 0) {
            mCountDownLatch = new CountDownLatch(task.dependencyTasks().size());
        }
    }

    /**
     * 添加本任务所依赖的某个任务
     *
     * @param wrapper
     */
    public void addDependencyWrapper(LaunchTaskWrapper wrapper) {
        if (!mDependencyWrapperList.contains(wrapper)) {
            mDependencyWrapperList.add(wrapper);
        }
    }

    public List<LaunchTaskWrapper> getDependencyWrapperList() {
        return mDependencyWrapperList;
    }


    public String getLaunchTaskClassName() {
        return mLaunchTask.getClass().getName();
    }

    public CountDownLatch getCountDownLatch() {
        return mCountDownLatch;
    }

    /**
     * 添加被依赖的任务的CountDownLatch
     *
     * @param countDownLatch
     */
    public void addByDependencyCountDownLatch(CountDownLatch countDownLatch) {
        if (!mByDependencyCountDownLatchList.contains(countDownLatch)) {
            mByDependencyCountDownLatchList.add(countDownLatch);
        }
    }

    /**
     * 本任务是否需要运行在主线程
     *
     * @return
     */
    public boolean isRunOnMainThread() {
        return mLaunchTask.isRunOnMainThread();
    }

    @Override
    public void run() {
        String taskClassName = mLaunchTask.getClass().getName();
        if (mLaunchTaskListener != null) {
            mLaunchTaskListener.onTaskStarted(taskClassName);
        }
        if (mCountDownLatch != null) {
            try {
                mCountDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mLaunchTask.run();
        if (mLaunchTaskListener != null) {
            mLaunchTaskListener.onTaskCompleted(taskClassName);
        }
        for (CountDownLatch countDownLatch : mByDependencyCountDownLatchList) {
            countDownLatch.countDown();
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LaunchTaskWrapper wrapper = (LaunchTaskWrapper) o;
        return Objects.equals(mLaunchTask, wrapper.mLaunchTask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mLaunchTask);
    }
}
