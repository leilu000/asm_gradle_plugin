package com.leilu.asm.gradle.thread_schedule;


import com.leilu.base.BaseExtensionInfo;

import java.util.LinkedList;
import java.util.List;

public class ThreadSchedule extends BaseExtensionInfo {

    /**
     * 配置需要hook的类的集合
     */
    public List<HookClass> hookClasses = new LinkedList<>();


    @Override
    public String getName() {
        return "thread_schedule";
    }

    public static class HookClass {
        /**
         * 需要hook的类名（com/xxx/xxx/xxx.class），为空则不hook
         */
        public String className;

        /**
         * hook的类下对应的方法信息
         * 规则：
         * 1、如果methods集合为空，则hook里面所有的方法
         * 2、如果HookMethod对象methodName和methodDesc同时为空，则不hook
         * 3、如果HookMethod对象的methodDesc为空，则hook所有和methodName同名的方法
         * 4、如果HookMethod对象的methodName为空，则hook所有和methodDesc同签名的方法
         * 5、如果methodDesc和methodName都不为空，则hook和methodName和methodDesc都匹配的方法
         */
        public List<HookMethod> methods = new LinkedList<>();
    }

    public static class HookMethod {

        /**
         * 方法签名
         */
        public String methodDesc;

        /**
         * 方法名
         */
        public String methodName;

        /**
         * 方法延时
         */
        public long delay;
    }
}
