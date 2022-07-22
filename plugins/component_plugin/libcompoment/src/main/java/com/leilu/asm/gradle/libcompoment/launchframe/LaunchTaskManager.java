package com.leilu.asm.gradle.libcompoment.launchframe;



import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTask;
import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTaskListener;
import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTaskSorter;
import com.leilu.asm.gradle.libcompoment.launchframe.base.IThreadPool;
import com.leilu.asm.gradle.libcompoment.launchframe.impl.DefaultLaunchThreadPool;
import com.leilu.asm.gradle.libcompoment.launchframe.impl.LaunchTaskSorterImpl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 负责任务调度的核心类
 */
public class LaunchTaskManager {

    // 负责任务排序的工具
    private final ILaunchTaskSorter mLaunchTaskSorter;
    // 线程池
    private IThreadPool mThreadPool;
    // 保存每个任务的入度数，key为任务对应的类名；value为对应的任务实体
    private final Map<String, LaunchTaskWrapper> mLaunchTaskMap = new HashMap<>();
    // 保存任务监听器
    private final List<ILaunchTaskListener> mLaunchTaskListenerList = new LinkedList<>();
    // 用于等待所有任务执行完成
    private CountDownLatch mCountDownLatch;
    // 用于标识任务是否开启
    private volatile boolean mIsStarted;
    // 用于标识任务是否全部完成
    private volatile boolean mHasCompleted;

    public LaunchTaskManager() {
        mLaunchTaskSorter = new LaunchTaskSorterImpl();
        mThreadPool = new DefaultLaunchThreadPool();
    }

    /**
     * 添加任务运行的监听器
     *
     * @param listener
     */
    public LaunchTaskManager addLaunchTaskListener(ILaunchTaskListener listener) {
        checkStartState();
        if (listener != null && !mLaunchTaskListenerList.contains(listener)) {
            mLaunchTaskListenerList.add(listener);
        }
        return this;
    }

    /**
     * 移除任务运行的监听器
     *
     * @param listener
     */
    public void removeLaunchTaskListener(ILaunchTaskListener listener) {
        mLaunchTaskListenerList.remove(listener);
    }

    /**
     * 设置线程池
     *
     * @param threadPool
     * @return
     */
    public LaunchTaskManager setThreadPool(IThreadPool threadPool) {
        checkStartState();
        if (threadPool != null) {
            mThreadPool = threadPool;
        }
        return this;
    }


    /**
     * 添加启动任务，会将该任务和该任务所依赖的任务都保存到字典中
     *
     * @param taskClass ILaunchTask的实现类
     * @return
     */
    public LaunchTaskManager addTask(Class<? extends ILaunchTask> taskClass) {
        checkStartState();
        addCreateAndAddDependencyWrapper(taskClass.getName());
        return this;
    }

    /**
     * 添加启动任务，会将该任务和该任务所依赖的任务都保存到字典中
     *
     * @param taskClass ILaunchTask的实现类
     * @return
     */
    public LaunchTaskManager addTask(String taskClass) {
        checkStartState();
        Class<?> clazz = Utils.getClassForName(taskClass);
        if (!ILaunchTask.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("The " + taskClass + " must be a child of the ILaunchTask interface !");
        }
        addTask((Class<? extends ILaunchTask>) clazz);
        return this;
    }

    /**
     * 启动任务（不会等待所有任务执行完成，如果需要等待所有任务执行完成，需要在调用此方法以后调用一下awaitAllTaskComplete方法）
     * 1、先根据添加的任务生成任务之间的依赖关系（有向无环图）
     * 2、根据有向芜湖安图列表，结合线程池来调度任务
     *
     * @return
     */
    public LaunchTaskManager start() {
        if (!mThreadPool.isMainThread()) {
            throw new RuntimeException("Please invoke on main thread !");
        }
        // 检查任务是否已经开始运行
        checkStartState();
        if (mLaunchTaskMap.size() == 0) {
            return this;
        }
        mCountDownLatch = new CountDownLatch(mLaunchTaskMap.size());
        // 生成任务的依赖关系的有向无环图
        LinkedList<LaunchTaskWrapper> tasks = mLaunchTaskSorter.sort(mLaunchTaskMap);
        // 根据生成的依赖关系集合来运行各个任务
        runTasks(tasks);
        mIsStarted = true;
        return this;
    }

    /**
     * 等待所有任务执行完成，需要在start以后调用
     */
    public void awaitAllTaskComplete() {
        if (!mIsStarted) {
            return;
        }
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 运行所有任务，先运行子线程的任务，因为如果先运行子线程的任务，会导致主线程
    // 任务阻塞，直到主线程任务全部执行完才能执行子线程的任务
    private void runTasks(LinkedList<LaunchTaskWrapper> tasks) {
        List<LaunchTaskWrapper> mainThreadTasks = new LinkedList<>();
        for (LaunchTaskWrapper wrapper : tasks) {
            if (wrapper.isRunOnMainThread()) {
                mainThreadTasks.add(wrapper);
            } else {
                mThreadPool.executeOnIOThread(wrapper);
            }
        }
        for (LaunchTaskWrapper wrapper : mainThreadTasks) {
            wrapper.run();
        }
    }


    // 检查任务是否已经启动了
    private void checkStartState() {
        if (mIsStarted) {
            throw new IllegalStateException("The task manager is started !");
        }
    }

    /**
     * 将对应任务的class类名包装成LaunchTaskWrapper对象，并生成任务之间的依赖
     * 关系
     *
     * @param taskClassName
     */
    private void addCreateAndAddDependencyWrapper(String taskClassName) {
        if (mLaunchTaskMap.containsKey(taskClassName)) {
            return;
        }
        ILaunchTask task = Utils.newLaunchTask(taskClassName);
        LaunchTaskWrapper wrapper = new LaunchTaskWrapper(task, mLaunchTaskListener);
        List<String> dependencyList = task.dependencyTasks();
        if (dependencyList != null) {
            for (String dependency : dependencyList) {
                ILaunchTask launchTask = Utils.newLaunchTask(dependency);
                LaunchTaskWrapper dependencyWrapper;
                if (mLaunchTaskMap.containsKey(dependency)) {
                    dependencyWrapper = mLaunchTaskMap.get(dependency);
                } else {
                    dependencyWrapper = new LaunchTaskWrapper(launchTask, mLaunchTaskListener);
                    mLaunchTaskMap.put(dependency, dependencyWrapper);
                }
                dependencyWrapper.addByDependencyCountDownLatch(wrapper.getCountDownLatch());
                wrapper.addDependencyWrapper(dependencyWrapper);
                addCreateAndAddDependencyWrapper(dependency);
            }
        }
        mLaunchTaskMap.put(taskClassName, wrapper);
    }

    private final ILaunchTaskListener mLaunchTaskListener = new ILaunchTaskListener() {
        @Override
        public void onTaskStarted(String taskName) {
            for (ILaunchTaskListener listener : mLaunchTaskListenerList) {
                listener.onTaskStarted(taskName);
            }
        }

        @Override
        public void onTaskCompleted(String taskName) {
            mCountDownLatch.countDown();
            for (ILaunchTaskListener listener : mLaunchTaskListenerList) {
                listener.onTaskCompleted(taskName);
            }
            if (!mHasCompleted && mCountDownLatch.getCount() == 0) {
                onAllTaskCompleted();
            }
        }

        @Override
        public void onAllTaskCompleted() {
            mHasCompleted = true;
            for (ILaunchTaskListener listener : mLaunchTaskListenerList) {
                listener.onAllTaskCompleted();
            }
        }
    };
}
