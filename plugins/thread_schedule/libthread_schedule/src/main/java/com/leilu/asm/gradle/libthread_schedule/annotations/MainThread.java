package com.leilu.asm.gradle.libthread_schedule.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
/**
 *  主线程执行
 */
public @interface MainThread {
    /**
     * 延迟的时间（毫秒），如果为0或者负数，则不延迟
     *
     * @return
     */
    long delay() default 0;

    /**
     * 超时返回时间（不包含延迟执行的delay），此属性只对有返回值的方法有效
     *
     * @return
     */
    long timeout() default 0;
}
