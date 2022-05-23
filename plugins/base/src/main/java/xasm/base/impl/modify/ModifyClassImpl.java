package xasm.base.impl.modify;

import xasm.ASMUtil;
import xasm.base.Const;
import xasm.base.impl.SimpleOnCoreAddMethodListener;
import xasm.base.impl.add.CoreAddFieldImpl;
import xasm.base.impl.add.CoreAddMethodImpl;
import xasm.base.impl.modify.bean.AddFieldData;
import xasm.base.impl.modify.bean.AddMethodData;
import xasm.base.impl.modify.bean.ModifyData;
import xasm.base.impl.modify.bean.PatchAddFieldData;
import xasm.base.impl.modify.field.ModifyFieldVisitor;
import xasm.base.impl.modify.field.ModifyMethodVisitor;
import xasm.base.inter.IAddAnnotation;
import xasm.base.inter.IAddField;
import xasm.base.inter.IAddMethod;
import xasm.base.inter.IHook;
import xasm.base.inter.IModifyClass;


import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 修改类的实现类
 * 功能点：
 * 1、添加类上的注解：如果以前有相同的注解，则删除旧的，添加新的
 * <p>
 * 2、添加属性：如果以前有相同属性，则删除旧的，添加新的
 * **********添加属性的同时添加注解
 * **********在已存在的属性上添加注解：如果以前有相同的注解，则删除旧的，添加新的
 * <p>
 * 3、添加属性上的注解：如果以前有相同的注解，则删除旧的，添加新的
 * <p>
 * 4、添加方法：如果以前有相同名字并且签名相同的方法，则删除旧的，添加新的
 * **********添加方法的同时添加注解
 * **********在已存在的方法上添加注解：如果以前有相同的注解，则删除旧的，添加新的
 * <p>
 * 5、对类进行hook，可以在方法执行前后出入代码，也可以动态替换某个方法的调用或者类的创建
 */
public class ModifyClassImpl implements IModifyClass {

    private final ClassWriter mClassWriter;
    private final byte[] mClassData;
    // 保存将要被移除的属性
    private final List<ModifyData> mPaddingRemoveFieldList = new ArrayList<>();
    // 保存将要被移除的方法
    private final List<ModifyData> mPaddingRemoveMethodList = new ArrayList<>();
    // 保存将要被移除的类上的注解
    private final List<String> mPaddingRemoveClassAnnotations = new ArrayList<>();
    // 保存将要被添加到类上的注解
    private Map<String, Map<String, Object>> mPaddingAddClassAnnotations;
    // 添加或者删除属性上的注解的帮助类
    private ModifyAnnotationHelper mFieldAnnotationHelper;
    // 添加或者删除方法上的注解的帮助类
    private ModifyAnnotationHelper mMethodAnnotationHelper;
    // 负责添加属性
    private final IAddField mAddField;
    // 保存将要被添加的属性
    private final List<AddFieldData> mPaddingAddFieldList = new ArrayList<>();
    // 保存将要被批量添加的属性
    private final List<PatchAddFieldData> mPaddingPatchAddFieldList = new ArrayList<>();
    // 负责添加方法
    private final IAddMethod mAddMethod;
    // 保存将要添加的方法
    private final List<AddMethodData> mPaddingAddMethodList = new ArrayList<>();
    // hook类的工具
    private IHook mHook;

    public ModifyClassImpl(String classPath) {
        this(ASMUtil.getClassData(classPath));
    }

    public ModifyClassImpl(byte[] classData) {
        if (classData == null) {
            throw new RuntimeException("The class is not exist !");
        }
        mClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        mClassData = classData;
        mAddField = new CoreAddFieldImpl(mClassWriter);
        mAddMethod = new CoreAddMethodImpl(mClassWriter);
    }


    @Override
    public byte[] toByteArray() {
        ClassReader cr = new ClassReader(mClassData);
        ClassVisitor classVisitor = new ClassVisitor(Const.ASM_VERSION, mClassWriter) {

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                // 这里遍历一下将要添加的属性是否已经在类中存在，如果存在，则添加到删除列表中删除旧的，等再visitEnd方法中添加新的
                if (mPaddingAddFieldList.contains(new AddFieldData(access, name, descriptor))) {
                    mPaddingRemoveFieldList.add(new ModifyData(name, descriptor));
                }
                // 这里先判断是否需要删除属性，如果是，则直接返回空，就没有必要再走下面的逻辑了
                if (mPaddingRemoveFieldList.contains(new ModifyData(name, descriptor))) {
                    return null;
                }
                createFieldAnnotationHelper();
                return new ModifyFieldVisitor(name, super.visitField(access, name, descriptor, signature, value)
                        , mFieldAnnotationHelper);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                // 判断如果类上已经存在了相同的注解，则删除旧的，添加新的
                if (mPaddingAddClassAnnotations != null) {
                    for (Map.Entry<String, Map<String, Object>> entry : mPaddingAddClassAnnotations.entrySet()) {
                        if (entry.getKey().equals(descriptor)) {
                            mPaddingRemoveClassAnnotations.add(entry.getKey());
                        }
                    }
                }

                for (String desc : mPaddingRemoveClassAnnotations) {
                    if (desc.equals(descriptor)) {
                        return null;
                    }
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                // 检查方法合法性
                ASMUtil.throwExceptionIfAbsOrNativeMethod(name, access);

                // 这里遍历一下将要添加的方法是否已经在类中存在，如果存在，则添加到删除列表中删除旧的，等再visitEnd方法中添加新的
                if (mPaddingAddMethodList.contains(new AddMethodData(access, name, descriptor))) {
                    mPaddingRemoveMethodList.add(new ModifyData(name, descriptor));
                }

                // 这里先判断是否需要删除方法，如果是，则直接返回空，就没有必要再走下面的逻辑了
                if (mPaddingRemoveMethodList.contains(new ModifyData(name, descriptor))) {
                    return null;
                }
                createMethodAnnotationHelper();
                return new ModifyMethodVisitor(name, descriptor, super.visitMethod(access, name, descriptor, signature, exceptions)
                        , mMethodAnnotationHelper);

            }

            private void addAnnotation(Object visitor, Map<String, Map<String, Object>> map) {
                if (map != null) {
                    ASMUtil.addAnnotations(visitor, new IAddField.OnAddFiledListener() {
                        @Override
                        public Map<String, Map<String, Object>> getAnnotationsKeyValue() {
                            return map;
                        }
                    }, new IAddAnnotation.OnStartAddAnnotationListener() {
                        @Override
                        public void onStartAddAnnotation(String annotationDescriber) {
                        }
                    });
                }
            }

            @Override
            public void visitEnd() {
                // 添加类级别的注解
                addAnnotation(mClassWriter, mPaddingAddClassAnnotations);
                // 添加新的属性
                for (AddFieldData data : mPaddingAddFieldList) {
                    mAddField.addField(data.access, data.name, data.describer, data.defaultValue, data.listener);
                }
                // 添加批量属性
                for (PatchAddFieldData data : mPaddingPatchAddFieldList) {
                    mAddField.addFields(data.access, data.varPrefix, data.types);
                }
                // 添加新的方法
                for (AddMethodData data : mPaddingAddMethodList) {
                    mAddMethod.addMethod(data.access, data.name, data.desc, data.exceptions, data.listener);
                }
                super.visitEnd();
            }
        };

        cr.accept(classVisitor, ClassReader.SKIP_DEBUG);

        // 对需要的方法进行hook
        if (mHook != null) {
            return mHook.startHook(mClassWriter.toByteArray());
        }
        return mClassWriter.toByteArray();
    }

    @Override
    public void removeField(String name) {
        ModifyData data = new ModifyData(name, null);
        if (!mPaddingRemoveFieldList.contains(data)) {
            mPaddingRemoveFieldList.add(data);
        }
    }

    @Override
    public void removeMethod(String name, String desc) {
        ModifyData data = new ModifyData(name, desc);
        if (!mPaddingRemoveMethodList.contains(data)) {
            mPaddingRemoveMethodList.add(data);
        }
    }

    @Override
    public void addFields(int access, String varPrefix, Type[] types) {
        PatchAddFieldData data = new PatchAddFieldData(access, varPrefix, types);
        if (!mPaddingPatchAddFieldList.contains(data)) {
            mPaddingPatchAddFieldList.add(data);
        }
    }

    @Override
    public void addField(int access, String name, String desc, Object defaultValue) {
        addField(access, name, desc, defaultValue, null);
    }

    @Override
    public void addField(int access, String name, String desc, Object defaultValue, IAddField.OnAddFiledListener listener) {
        AddFieldData data = new AddFieldData(access, name, desc, defaultValue, listener);
        if (!mPaddingAddFieldList.contains(data)) {
            mPaddingAddFieldList.add(data);
        }
    }

    @Override
    public void addFieldAnnotation(String name, Map<String, Map<String, Object>> annotationMap) {
        createFieldAnnotationHelper();
        mFieldAnnotationHelper.addAnnotation(name, annotationMap);
    }

    @Override
    public void removeFieldAnnotation(String name, String[] annotationClasses) {
        createFieldAnnotationHelper();
        mFieldAnnotationHelper.removeAnnotation(name, annotationClasses);
    }

    @Override
    public void addMethodAnnotation(String methodName, String desc, Map<String, Map<String, Object>> annotationMap) {
        createMethodAnnotationHelper();
        mMethodAnnotationHelper.addAnnotation(methodName + desc, annotationMap);
    }


    @Override
    public void removeMethodAnnotation(String name, String desc, String[] annotationClasses) {
        createMethodAnnotationHelper();
        mMethodAnnotationHelper.removeAnnotation(name + desc, annotationClasses);
    }

    @Override
    public void removeClassAnnotation(String[] annotationClasses) {
        if (annotationClasses == null) {
            throw new IllegalArgumentException("The remove annotation class array must be not null !");
        }
        mPaddingRemoveClassAnnotations.addAll(new ArrayList<>(Arrays.asList(annotationClasses)));
    }

    @Override
    public void addClassAnnotation(Map<String, Map<String, Object>> annotations) {
        mPaddingAddClassAnnotations = annotations;
    }

    @Override
    public void addConstructorMethod(int access, String desc, SimpleOnCoreAddMethodListener listener) {

    }

    @Override
    public void addConstructorMethod(int access, String desc, String[] exceptions, SimpleOnCoreAddMethodListener listener) {

    }

    @Override
    public void addMethod(int access, String name, String desc, SimpleOnCoreAddMethodListener listener) {
        addMethod(access, name, desc, null, listener);
    }

    @Override
    public void addMethod(int access, String name, String desc, String[] exceptions, SimpleOnCoreAddMethodListener listener) {
        AddMethodData data = new AddMethodData(access, name, desc, exceptions, listener);
        if (!mPaddingAddMethodList.contains(data)) {
            mPaddingAddMethodList.add(data);
        }
    }

    @Override
    public void hookMethod(String methodName, Class<?> returnType, Class<?>[] parameterTypes, OnHookMethodListener listener) {
        initHook();
        mHook.hookMethod(methodName, returnType, parameterTypes, listener);
    }

    @Override
    public void hookMethod(String methodName, String describer, OnHookMethodListener listener) {
        initHook();
        mHook.hookMethod(methodName, describer, listener);
    }

    @Override
    public void hookMethod(String methodName, String describer, String interfaceName, OnHookMethodListener listener) {
        initHook();
        mHook.hookMethod(methodName, describer, interfaceName, listener);
    }

    @Override
    public void hookMethodWithAnnotations(List<String> annotationDescriber, OnHookMethodWithAnnotationListener listener) {
        initHook();
        mHook.hookMethodWithAnnotations(annotationDescriber, listener);
    }

    @Override
    public byte[] startHook(byte[] sourceData) {
        throw new IllegalStateException("This method is not required here !");
    }

    private void createFieldAnnotationHelper() {
        if (mFieldAnnotationHelper == null) {
            mFieldAnnotationHelper = new ModifyAnnotationHelper();
        }
    }

    private void createMethodAnnotationHelper() {
        if (mMethodAnnotationHelper == null) {
            mMethodAnnotationHelper = new ModifyAnnotationHelper();
        }
    }

    private void initHook() {
        if (mHook == null) {
            mHook = new HookImpl();
        }
    }

}
