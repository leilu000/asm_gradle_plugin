package xasm;

import xasm.base.impl.SimpleOnCoreAddMethodListener;
import xasm.base.impl.modify.ModifyClassImpl;
import xasm.base.impl.modify.bean.ResultInfo;
import xasm.base.inter.IAddAnnotation;
import xasm.base.inter.IAddField;
import xasm.base.inter.IHook;
import xasm.base.inter.IModifyClass;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 修改类的包装类，主要是方便链式调用
 */
public class ModifyClassWrapper {

    private final IModifyClass mModifyASM;

    public ModifyClassWrapper(String classPath) {
        this(ASMUtil.getClassData(classPath));
    }

    public ModifyClassWrapper(byte[] classData) {
        mModifyASM = new ModifyClassImpl(classData);
    }

    public ResultInfo toByteArray() {
        return mModifyASM.toByteArray();
    }

    /**
     * 删除属性
     *
     * @param name 属性名
     * @return
     */
    public ModifyClassWrapper removeField(String name) {
        mModifyASM.removeField(name);
        return this;
    }

    /**
     * 批量删除属性上的注解
     *
     * @param name        属性名
     * @param annotations 注解集合，如果为空则全部删除
     * @return
     */
    public ModifyClassWrapper removeFieldAnnotation(String name, String[] annotations) {
        mModifyASM.removeFieldAnnotation(name, annotations);
        return this;
    }

    /**
     * 删除某个属性的所有注解
     *
     * @param name 属性名
     * @return
     */
    public ModifyClassWrapper removeFieldAnnotation(String name) {
        removeFieldAnnotation(name, null);
        return this;
    }

    /**
     * 添加属性上的注解
     *
     * @param name     属性名
     * @param listener 复写返回返回需要添加的属性集合
     * @return
     */
    public ModifyClassWrapper addFieldAnnotation(String name, IAddAnnotation listener) {
        if (listener != null) {
            mModifyASM.addFieldAnnotation(name, listener.getAnnotationsKeyValue());
        }
        return this;
    }

    /**
     * 删除方法
     *
     * @param name 方法名
     * @param desc 方法签名
     * @return
     */
    public ModifyClassWrapper removeMethod(String name, String desc) {
        mModifyASM.removeMethod(name, desc);
        return this;
    }

    /**
     * 删除方法上的注解
     *
     * @param name        方法名
     * @param desc        返回值类型
     * @param annotations 注解集合，不能为空
     * @return
     */
    public ModifyClassWrapper removeMethodAnnotation(String name, String desc, String[] annotations) {
        if (annotations == null || annotations.length == 0) {
            throw new IllegalArgumentException("The annotations is null");
        }
        mModifyASM.removeMethodAnnotation(name, desc, annotations);
        return this;
    }

    /**
     * 添加方法上的注解
     *
     * @param name     方法名
     * @param desc     注解签名
     * @param listener 复写返回返回需要添加的属性集合
     * @return
     */
    public ModifyClassWrapper addMethodAnnotation(String name, String desc, IAddAnnotation listener) {
        if (listener != null) {
            mModifyASM.addMethodAnnotation(name, desc, listener.getAnnotationsKeyValue());
        }
        return this;
    }

    /**
     * 删除类上的注解，为空则全部删除
     *
     * @param annotations
     * @return
     */
    public ModifyClassWrapper removeClassAnnotation(String[] annotations) {
        mModifyASM.removeClassAnnotation(annotations);
        return this;
    }

    /**
     * 添加类上的属性
     *
     * @param listener 复写返回返回需要添加的属性集合
     * @return
     */
    public ModifyClassWrapper addClassAnnotation(IAddAnnotation listener) {
        if (listener != null) {
            mModifyASM.addClassAnnotation(listener.getAnnotationsKeyValue());
        }
        return this;
    }

    /**
     * 添加属性
     *
     * @param access       Opcodec.ACC_PUBLIC...
     * @param name         属性名
     * @param desc         属性签名
     * @param defaultValue 默认值
     * @param listener     复写方法，是否添加注解等
     * @return
     */
    public ModifyClassWrapper addField(int access, String name, String desc, Object defaultValue, IAddField.OnAddFiledListener listener) {
        mModifyASM.addField(access, name, desc, defaultValue, listener);
        return this;
    }

    /**
     * 添加属性
     *
     * @param access       Opcodec.ACC_PUBLIC...
     * @param name         属性名
     * @param desc         属性签名
     * @param defaultValue 默认值
     * @return
     */
    public ModifyClassWrapper addField(int access, String name, String desc, Object defaultValue) {
        mModifyASM.addField(access, name, desc, defaultValue, null);
        return this;
    }

    /**
     * 添加构造方法
     *
     * @param access   Opcodec.ACC_PUBLIC...
     * @param desc     方法签名
     * @param listener 添加方法体等
     * @return
     */
    public ModifyClassWrapper addConstructorMethod(int access, String desc, SimpleOnCoreAddMethodListener listener) {
        addConstructorMethod(access, desc, null, listener);
        return this;
    }

    /**
     * 添加构造方法
     *
     * @param access     Opcodec.ACC_PUBLIC...
     * @param desc       方法签名
     * @param exceptions 异常集合
     * @param listener   添加方法体等
     * @return
     */
    public ModifyClassWrapper addConstructorMethod(int access, String desc, String[] exceptions, SimpleOnCoreAddMethodListener listener) {
        mModifyASM.addConstructorMethod(access, desc, exceptions, listener);
        return this;
    }

    /**
     * 添加方法
     *
     * @param access   Opcodec.ACC_PUBLIC...
     * @param name     方法名
     * @param desc     方法签名
     * @param listener 添加方法体等
     * @return
     */
    public ModifyClassWrapper addMethod(int access, String name, String desc, SimpleOnCoreAddMethodListener listener) {
        addMethod(access, name, desc, null, listener);
        return this;
    }

    /**
     * 添加方法
     *
     * @param access     Opcodec.ACC_PUBLIC...
     * @param name       方法名
     * @param desc       方法签名
     * @param exceptions 异常数组
     * @param listener   添加方法体等
     * @return
     */
    public ModifyClassWrapper addMethod(int access, String name, String desc, String[] exceptions, SimpleOnCoreAddMethodListener listener) {
        mModifyASM.addMethod(access, name, desc, exceptions, listener);
        return this;
    }

    /**
     * hook某个方法
     *
     * @param methodName     方法名
     * @param returnType     返回值类型，没有返回值则为null
     * @param parameterTypes 方法参数，没有参数则为null
     * @param listener       hook方法的回调
     */
    public ModifyClassWrapper hookMethod(String methodName, Class<?> returnType, Class<?>[] parameterTypes
            , IHook.OnHookMethodListener listener) {
        mModifyASM.hookMethod(methodName, returnType, parameterTypes, listener);
        return this;
    }

    /**
     * hook某个方法，不兼容lambda表达式，如果需要hook lambda表达式，则使用 调用interfaceName参数的hookMethod方法
     *
     * @param methodName 方法名
     * @param describer  方法签名
     * @param listener   hook方法的回调
     */
    public ModifyClassWrapper hookMethod(String methodName, String describer
            , IHook.OnHookMethodListener listener) {
        hookMethod(methodName, describer, null, listener);
        return this;
    }

    /**
     * hook某个方法，并且兼容lambda表达式
     *
     * @param methodName    方法名
     * @param describer     方法签名
     * @param interfaceName 方法所在接口，如果传null或者""，则不会hook lambda表达式的方法
     *                      需要填全类名，比如：java/langRunnable、android/view$OnClickListener等
     * @param listener      hook方法的回调
     */
    public ModifyClassWrapper hookMethod(String methodName, String describer, String interfaceName
            , IHook.OnHookMethodListener listener) {
        mModifyASM.hookMethod(methodName, describer, interfaceName, listener);
        return this;
    }

    /**
     * hook带有某些注解的方法
     *
     * @return
     */
    public ModifyClassWrapper hookMethodWidthAnnotation(Class<?>[] annotationClass, IHook.OnHookMethodWithAnnotationListener listener) {
        int len = annotationClass.length;
        String[] annotationDescriber = new String[len];
        for (int i = 0; i < len; i++) {
            annotationDescriber[i] = ASMUtil.getDescriberByClass(annotationClass[i]);
        }
        hookMethodWidthAnnotation(annotationDescriber, listener);
        return this;
    }

    /**
     * hook带有某些注解的方法
     *
     * @return
     */
    public ModifyClassWrapper hookMethodWidthAnnotation(String[] annotationDescriber, IHook.OnHookMethodWithAnnotationListener listener) {
        if (annotationDescriber == null || annotationDescriber.length == 0) {
            throw new IllegalArgumentException("The annotation is null !");
        }
        mModifyASM.hookMethodWithAnnotations(new ArrayList<>(Arrays.asList(annotationDescriber)), listener);
        return this;
    }
}
