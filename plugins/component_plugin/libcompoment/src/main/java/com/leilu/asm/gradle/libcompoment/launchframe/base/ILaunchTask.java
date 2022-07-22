package com.leilu.asm.gradle.libcompoment.launchframe.base;

import java.util.List;

public interface ILaunchTask extends Runnable {

    boolean isRunOnMainThread();

    List<String> dependencyTasks();

}
