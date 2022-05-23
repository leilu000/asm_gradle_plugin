package com.leilu.asm.gradle.thread_schedule;

public interface Const {

    String FULL_ANNOTATION_CLASS_PATH = "com/leilu/asm/gradle/libthread_schedule/annotations";

    String CLASS_NAME_THREAD_SCHEDULE_UTIL = "com/leilu/asm/gradle/libthread_schedule/ThreadScheduleUtil";
    String CLASS_NAME_MAIN_THREAD = (FULL_ANNOTATION_CLASS_PATH + ".MainThread").replaceAll("/", ".");
    String CLASS_NAME_BG_THREAD = (FULL_ANNOTATION_CLASS_PATH + ".BGThread").replaceAll("/", ".");
    String CLASS_NAME_TRY_CATCH_COUNTDOWN_LATCH = "com/leilu/asm/gradle/libthread_schedule/TryCatchCountDownLatch";
    String DESC_ANNOTATION_MAIN_THREAD = "L" + FULL_ANNOTATION_CLASS_PATH + "/MainThread;";
    String DESC_ANNOTATION_BG_THREAD = "L" + FULL_ANNOTATION_CLASS_PATH + "/BGThread;";
    String DESC_METHOD_GET_INSTANCE = "()L" + CLASS_NAME_THREAD_SCHEDULE_UTIL + ";";
    String DESC_METHOD_RUN_ON_BG_THREAD = "(JLjava/lang/Runnable;)V";
    String DESC_METHOD_RUN_ON_MAIN_THREAD = "(JLjava/lang/Runnable;)V";
    String DESC_METHOD_IS_MAIN_THREAD = "()Z";
    String DESC_COUNT_DOWN_LATCH = "L" + CLASS_NAME_TRY_CATCH_COUNTDOWN_LATCH + ";";
    String DESC_METHOD_COUNT_DOWN_LATCH = "()V";
    String DESC_METHOD_SET_COUNT_DOWN_LATCH = "(" + DESC_COUNT_DOWN_LATCH + ")V";
    String DESC_METHOD_AWAIT = "()V";

    String METHOD_GET_INSTANCE = "getInstance";
    String METHOD_RUN_ON_BG_THREAD = "runOnBGThread";
    String METHOD_RUN_ON_MAIN_THREAD = "runOnMainThread";
    String METHOD_IS_MAIN_THREAD = "isMainThread";
    String METHOD_SET_COUNT_DOWN_LATCH = "setCountDownLatch";
    String METHOD_COUNT_DOWN = "countDown";
    String METHOD_AWAIT = "await";

    String ANNOTATION_KEY_DELAY = "delay";

    String FIELD_COUNT_DOWN_LATCH = "countDownLatch";
    String FIELD_RESULT = "result";

}
