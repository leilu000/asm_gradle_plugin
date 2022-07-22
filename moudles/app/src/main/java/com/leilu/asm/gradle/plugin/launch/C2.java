package com.leilu.asm.gradle.plugin.launch;

import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTask;

import java.util.List;

public class C2 implements ILaunchTask {

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

    }
}
