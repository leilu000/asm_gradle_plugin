package com.leilu.asm.gradle.plugin.launch;

import android.os.SystemClock;

import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTask;

import java.util.List;

public class A0 implements ILaunchTask {

    @Override
    public boolean isRunOnMainThread() {
        return false;
    }

    @Override
    public List<String> dependencyTasks() {
        return null;
    }

    @Override
    public void run() {
        System.out.println("开始执行" + getClass().getName());
        SystemClock.sleep(5000);
        System.out.println("执行完成了=======:" + getClass().getName());
    }
}
