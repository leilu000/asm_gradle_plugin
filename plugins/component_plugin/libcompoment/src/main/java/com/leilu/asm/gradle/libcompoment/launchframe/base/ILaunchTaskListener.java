package com.leilu.asm.gradle.libcompoment.launchframe.base;

public interface ILaunchTaskListener {


    void onTaskStarted(String taskName);

    void onTaskCompleted(String taskName);

    void onAllTaskCompleted();

}
