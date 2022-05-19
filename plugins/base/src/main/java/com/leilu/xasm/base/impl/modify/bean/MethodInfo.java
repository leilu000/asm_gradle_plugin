package com.leilu.xasm.base.impl.modify.bean;

import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

public class MethodInfo {

    /**
     * 访问权限、方法属性（静态、接口、抽象方法等）
     */
    public int access;

    /**
     * 方法描述符
     */
    public String desc;

    /**
     * 方法名
     */
    public String name;

    /**
     * 参数类型从左到右）
     */
    public Type[] parameterTypes = new Type[0];

    /**
     * 是否是静态方法
     */
    public boolean isStatic;

    /**
     * 如果方法有注解，则返回注解的键值对,key：注解类名 xxx.xxx.xx  value：注解对应的值
     */
    public Map<String, Map<String, Object>> annotationMap = new HashMap<>();

}
