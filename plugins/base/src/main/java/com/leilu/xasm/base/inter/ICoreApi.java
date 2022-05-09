package com.leilu.xasm.base.inter;

import org.objectweb.asm.MethodVisitor;

/**
 * 使用core api的方式来调用方法或者new对象等操作
 */
public interface ICoreApi {
    /**
     * core api的方式在方法中new一个对象
     *
     * @param mv
     * @param clazz            对象类型
     * @param paramTypes       构造方法参数类型，必须和paramSlotIndexes一一对应
     * @param paramSlotIndexes 构造方法参数在局部变量表中的下标,必须和paramTypes一一对应
     */
    void newCoreClass1(MethodVisitor mv, Class<?> clazz, Class<?>[] paramTypes, int[] paramSlotIndexes);

    /**
     * core api的方式在方法中new一个对象
     *
     * @param mv
     * @param clazz  对象类型
     * @param params 构造方法参数类型，如果为空，则不传参数
     */
    void newCoreClass2(MethodVisitor mv, Class<?> clazz, Object[] params);


    /**
     * core api的方式在方法调用一个对象的方法
     *
     * @param mv
     * @param owner      方法所在的类
     * @param name       方法名
     * @param returnType 返回值类型，没有设置为null
     * @param paramTypes 参数类型，没有设置为null
     * @param params     参数值，和paramTypes一一对应
     * @param isStatic   是否是静态方法，true为是
     */
    void invokeCoreMethod1(MethodVisitor mv, Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes
            , Object[] params, boolean isStatic);

    /**
     * core api的方式在方法调用一个对象的方法
     *
     * @param mv
     * @param owner            方法所在的类
     * @param name             方法名
     * @param returnType       返回值类型，没有设置为null
     * @param paramTypes       参数类型（必须和paramSlotIndexes一一对应），没有设置为null
     * @param paramSlotIndexes 参数类型（必须和paramTypes一一对应），没有设置为null
     * @param isStatic         是否是静态方法，true为是
     */
    void invokeCoreMethod2(MethodVisitor mv, Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes
            , int[] paramSlotIndexes, boolean isStatic);

    /**
     * core api的方式访问某个类的属性
     *
     * @param mv
     * @param owner    所在类
     * @param name     属性名
     * @param type     属性类型
     * @param isStatic 是否是静态属性
     */
    void getCoreField(MethodVisitor mv, Class<?> owner, String name, Class<?> type, boolean isStatic);

}
