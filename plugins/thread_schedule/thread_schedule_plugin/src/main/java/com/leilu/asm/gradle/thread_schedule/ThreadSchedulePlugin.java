package com.leilu.asm.gradle.thread_schedule;

import com.android.build.api.transform.Status;
import com.leilu.base.BasePlugin;

import java.util.jar.JarOutputStream;

import xasm.XASM;

public class ThreadSchedulePlugin extends BasePlugin<ThreadSchedule> {

    @Override
    protected byte[] modifyJarInputClass(String className, JarOutputStream jos, byte[] sourceData, Status status) {
        return modifyClass(className, null, jos, sourceData, status);
    }

    @Override
    protected byte[] modifyDirectorInputClass(String className, String destDir, byte[] sourceData, Status status) {
        return modifyClass(className, destDir, null, sourceData, status);
    }

    private byte[] modifyClass(String className, String destDir, JarOutputStream jos, byte[] classData, Status status) {
        return XASM.getInstance()
                .modifyClass(classData)
                // hook BGThread和MainThread这两个注解
                .hookMethodWithAnnotation(new String[]{Const.DESC_ANNOTATION_BG_THREAD, Const.DESC_ANNOTATION_MAIN_THREAD}
                        , new ThreadScheduleHooker(destDir, jos, mExtension))
                .toByteArray().data;
    }

    @Override
    protected ThreadSchedule initSelfDefineExtension() {
        return new ThreadSchedule();
    }
}
