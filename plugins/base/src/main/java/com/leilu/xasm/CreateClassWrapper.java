package com.leilu.xasm;

import com.leilu.xasm.base.impl.SimpleOnAddMethodListener;
import com.leilu.xasm.base.impl.add.CreateClassImpl;
import com.leilu.xasm.base.inter.IAddAnnotation;
import com.leilu.xasm.base.inter.IAddField;
import com.leilu.xasm.base.inter.ICreateClass;
import org.objectweb.asm.Opcodes;

/**
 * 创建类的包装类，主要是方便链式调用
 */
public class CreateClassWrapper {


    private final ICreateClass mAddASM;

    /**
     * 默认会创建无参构造方法，并没有方法体
     *
     * @param className 类名
     */
    public CreateClassWrapper(String className) {
        this(className, null, null);
    }

    /**
     * 默认会创建无参构造方法
     *
     * @param className 类名
     * @param listener  根据需要复写响应方法类创建方法体等
     */
    public CreateClassWrapper(String className, SimpleOnAddMethodListener listener) {
        this(className, null, listener);
    }

    /**
     * 默认会创建无参构造方法
     *
     * @param className  类名
     * @param superClass 父类
     * @param listener   根据需要复写响应方法类创建方法体等
     */
    public CreateClassWrapper(String className, Class<?> superClass, SimpleOnAddMethodListener listener) {
        this(Opcodes.V1_8, className, superClass, listener);
    }

    /**
     * 默认会创建无参构造方法
     *
     * @param javaVersion java版本: Opcodes.V1_8等
     * @param className   类名
     * @param superClass  父类
     * @param listener    根据需要复写响应方法类创建方法体等
     */
    public CreateClassWrapper(int javaVersion, String className, Class<?> superClass, SimpleOnAddMethodListener listener) {
        mAddASM = new CreateClassImpl(javaVersion, className, superClass, listener);
    }

    /**
     * 添加构造方法
     *
     * @param access   访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param desc     方法签名
     * @param listener 根据需要复写响应方法类创建方法体等
     * @return
     */
    public CreateClassWrapper addConstructorMethod(int access, String desc, SimpleOnAddMethodListener listener) {
        addConstructorMethod(access, desc, null, listener);
        return this;
    }

    /**
     * 添加构造方法
     *
     * @param access     访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param desc       方法签名
     * @param exceptions 异常数组，没有设置为null
     * @param listener   根据需要复写响应方法类创建方法体等
     * @return
     */
    public CreateClassWrapper addConstructorMethod(int access, String desc, Class<?>[] exceptions
            , SimpleOnAddMethodListener listener) {
        mAddASM.addConstructorMethod(access, desc, exceptions, listener);
        return this;
    }

    /**
     * 添加方法（不带抛出异常）
     *
     * @param access   访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param name     方法名
     * @param desc     方法签名
     * @param listener 根据需要复写响应方法类创建方法体等
     * @return
     */
    public CreateClassWrapper addMethod(int access, String name, String desc, SimpleOnAddMethodListener listener) {
        addMethod(access, name, desc, null, listener);
        return this;
    }

    /**
     * 添加方法（带有抛出异常）
     *
     * @param access     访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param name       方法名
     * @param desc       方法签名
     * @param exceptions 异常数组，没有设置为null
     * @param listener   根据需要复写响应方法类创建方法体等
     * @return
     */
    public CreateClassWrapper addMethod(int access, String name, String desc
            , Class<?>[] exceptions, SimpleOnAddMethodListener listener) {
        mAddASM.addMethod(access, name, desc, exceptions, listener);
        return this;
    }

    /**
     * 添加属性，并添加注解
     *
     * @param access       访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param name         属性名
     * @param desc         方法签名
     * @param defaultValue 默认值
     * @param listener     添加注解的回调,在里面返回需要添加的属性的数组
     * @return
     */
    public CreateClassWrapper addField(int access, String name, String desc, Object defaultValue
            , IAddField.OnAddFiledListener listener) {
        mAddASM.addField(access, name, desc, defaultValue, listener);
        return this;
    }

    /**
     * 添加属性
     *
     * @param access       访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param name         属性名
     * @param desc         方法签名
     * @param defaultValue 默认值
     * @return
     */
    public CreateClassWrapper addField(int access, String name, String desc, Object defaultValue) {
        mAddASM.addField(access, name, desc, defaultValue, null);
        return this;
    }

    /**
     * 添加类的注解
     *
     * @param listener
     * @return
     */
    public CreateClassWrapper addAnnotation(IAddAnnotation listener) {
        mAddASM.addAnnotation(listener);
        return this;
    }

    /**
     * 获取最终结果数据
     *
     * @return
     */
    public byte[] toByteArray() {
        return mAddASM.toByteArray();
    }
}
