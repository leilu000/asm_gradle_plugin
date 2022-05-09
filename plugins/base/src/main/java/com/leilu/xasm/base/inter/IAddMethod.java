package com.leilu.xasm.base.inter;


import com.leilu.xasm.base.impl.SimpleOnAddMethodListener;
import org.objectweb.asm.MethodVisitor;

/**
 * 和添加方法相关的接口
 */
public interface IAddMethod {

    interface OnAddMethodListener extends IAddAnnotation {
        /**
         * 添加具体的方法内容，已经处理好方法的开始和结束，只需要添加对应的方法体内容即可
         * 无需再调用visitCode
         * 无需再调用visitInsn(Opcodec.XXRETURN)
         * 无需再调用visitMaxs方法
         *
         * @param mv
         */
        void onAddMethodBody(MethodVisitor mv);


    }

    /**
     * 添加构造方法
     *
     * @param access         Opcodec.ACC_PUBLIC...
     * @param desc 方法签名
     * @param listener
     */
    void addConstructorMethod(int access, String desc, SimpleOnAddMethodListener listener);

    /**
     * 添加构造方法
     *
     * @param access     Opcodec.ACC_PUBLIC...
     * @param desc       方法签名
     * @param exceptions 异常集合
     * @param listener
     */
    void addConstructorMethod(int access, String desc, Class<?>[] exceptions, SimpleOnAddMethodListener listener);

    /**
     * 添加方法，不带抛出异常
     *
     * @param access Opcodec.ACC_PUBLIC...
     * @param name   方法名
     * @param desc   方法签名
     */
    void addMethod(int access, String name, String desc, SimpleOnAddMethodListener listener);

    /**
     * 添加方法，带有抛出异常
     *
     * @param access     Opcodec.ACC_PUBLIC...
     * @param name       方法名
     * @param desc       方法签名
     * @param exceptions 异常数组
     */
    void addMethod(int access, String name, String desc, Class<?>[] exceptions, SimpleOnAddMethodListener listener);

}
