package com.leilu.asm.gradle.thread_schedule;

public interface Const {

    String THREAD_SCHEDULE_UTIL_CLASS = "com/leilu/asm/gradle/libthread_schedule/ThreadScheduleUtil";

    String METHOD_GET_INSTANCE = "getInstance";
    String METHOD_RUN_ON_BG_THREAD = "runOnBGThread";
    String METHOD_RUN_ON_MAIN_THREAD = "runOnMainThread";

    String DESC_METHOD_GET_INSTANCE = "L" + THREAD_SCHEDULE_UTIL_CLASS + ";";
    String DESC_METHOD_RUN_ON_BG_THREAD = "(JLjava/lang/Runnable;)V";
    String DESC_METHOD_RUN_ON_MAIN_THREAD = "(JLjava/lang/Runnable;)V";

    String DESC_ANNOTATION_PATH = "com/leilu/asm/gradle/libthread_schedule/annotations";
    String DESC_ANNOTATION_MAIN_THREAD = DESC_ANNOTATION_PATH + "/MainThread";
    String DESC_ANNOTATION_BG_THREAD = DESC_ANNOTATION_PATH + "/BGThread";

}
