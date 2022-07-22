package com.leilu.asm.gradle.libcompoment.launchframe;


import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTask;

public class Utils {


    public static ILaunchTask newLaunchTask(Class<? extends ILaunchTask> taskClass) {
        try {
            return taskClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ILaunchTask newLaunchTask(String launchTaskClassStr) {
        try {
            return (ILaunchTask) getClassForName(launchTaskClassStr).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getClassForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
