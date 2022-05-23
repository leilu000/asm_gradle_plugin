package xasm.base.inter;

import org.objectweb.asm.tree.InsnList;

/**
 * 使用tree api的方式来调用方法或者new对象等操作
 */
public interface ITreeApi {

    /**
     * tree api的方式new一个对象
     *
     * @param clazz
     * @param paramTypes
     * @param paramSlotIndexes
     * @return
     */
    InsnList newTreeClass1(Class<?> clazz, Class<?>[] paramTypes, int[] paramSlotIndexes);

    /**
     * tree api的方式new一个对象
     *
     * @param clazz
     * @param paramTypes
     * @param params
     * @return
     */
    InsnList newTreeClass2(Class<?> clazz, Class<?>[] paramTypes, Object[] params);


    /**
     * 调用一个对象的方法
     *
     * @param owner      方法所在的类
     * @param name       方法名
     * @param returnType 返回值类型，没有设置为null
     * @param paramTypes 参数类型，没有设置为null
     * @param params     参数类型（必须和paramTypes一一对应），没有设置为null
     * @param isStatic   是否是静态方法，true为是
     */
    InsnList invokeTreeMethod1(Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes
            , Object[] params, boolean isStatic);

    /**
     * 调用一个对象的方法
     *
     * @param owner            方法所在的类
     * @param name             方法名
     * @param returnType       返回值类型，没有设置为null
     * @param paramTypes       参数类型（必须和paramSlotIndexes一一对应），没有设置为null
     * @param paramSlotIndexes 参数类型（必须和paramTypes一一对应），没有设置为null
     * @param isStatic         是否是静态方法，true为是
     */
    InsnList invokeTreeMethod2(Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes
            , int[] paramSlotIndexes, boolean isStatic);

    /**
     * 访问某个类的属性
     *
     * @param owner    所在类
     * @param name     属性名
     * @param type     属性类型
     * @param isStatic 是否是静态属性
     */
    InsnList getTreeField(Class<?> owner, String name, Class<?> type, boolean isStatic);


}
