package com.leilu.asm.gradle.plugin.launch;

import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTask;

import java.util.ArrayList;
import java.util.List;

public class F5 implements ILaunchTask {

    @Override
    public boolean isRunOnMainThread() {
        return false;
    }

    @Override
    public List<String> dependencyTasks() {
        List<String> list = new ArrayList<>();
        list.add(D3.class.getName());
        list.add(E4.class.getName());
        return list;
    }

    @Override
    public void run() {
        System.out.println("开始执行" + getClass().getName());
        System.out.println(1000);
        System.out.println("执行完成了=======:" + getClass().getName());
    }
}
