package com.leilu.asm.gradle.thread_schedule;

import com.android.build.api.transform.Status;
import com.leilu.base.BasePlugin;

import xasm.XASM;

public class ThreadSchedulePlugin extends BasePlugin<ThreadSchedule> {
    @Override
    protected byte[] modifyClass(String destDirPath, byte[] classData, Status status) {
        return XASM.getInstance()
                .modifyClass(classData)
                // hook BGThread和MainThread这两个注解
                .hookMethodWidthAnnotation(new String[]{Const.DESC_ANNOTATION_BG_THREAD, Const.DESC_ANNOTATION_MAIN_THREAD}
                        , new ThreadScheduleHooker(destDirPath, mExtension))
                .toByteArray();
    }

    @Override
    protected ThreadSchedule initSelfDefineExtension() {
        return new ThreadSchedule();
    }
}
