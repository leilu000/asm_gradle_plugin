package com.leilu.xasm.base.impl.add;

import com.leilu.xasm.ASMUtil;
import com.leilu.xasm.base.impl.SimpleOnAddMethodListener;
import com.leilu.xasm.base.Const;
import com.leilu.xasm.base.inter.IAddAnnotation;
import com.leilu.xasm.base.inter.IAddField;
import com.leilu.xasm.base.inter.IAddMethod;
import com.leilu.xasm.base.inter.ICreateClass;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 创建类的实现类
 */
public class CreateClassImpl implements ICreateClass {

    private final ClassWriter mClassWriter;
    // 负责添加方法的类
    private final IAddMethod mAddMethodImpl;
    // 负责添加属性的类
    private final IAddField mAddFieldImpl;
    private final String mSuperClassName;
    private final String mClassName;

    /**
     * 创建一个类，并创建一个午餐空构造方法，并没有方法体
     *
     * @param className 类名
     */
    public CreateClassImpl(String className) {
        this(className, null, null);
    }

    /**
     * 创建一个类，并创建一个午餐空构造方法，会通过SimpleOnAddMethodListener回调实现自己的方法体
     *
     * @param className 类名
     * @param listener  在回调里面写自己的方法体
     */
    public CreateClassImpl(String className, SimpleOnAddMethodListener listener) {
        this(className, null, listener);
    }

    /**
     * 创建一个类，并创建一个带有父类的无餐构造方法，并没有方法体
     *
     * @param className  类名
     * @param superClass 父类
     */
    public CreateClassImpl(String className, Class<?> superClass) {
        this(className, superClass, null);
    }

    /**
     * 创建一个类，并创建一个带有父类的无餐构造方法，会通过SimpleOnAddMethodListener回调实现自己的方法体
     *
     * @param className  类名
     * @param superClass 父类
     * @param listener   在回调里面写自己的方法体
     */
    public CreateClassImpl(String className, Class<?> superClass, SimpleOnAddMethodListener listener) {
        this(Opcodes.V1_8, className, superClass, listener);
    }

    /**
     * 创建一个包含java版本的带有父类的构造方法
     *
     * @param javaVersion java版本：        Opcodes.V1_8。。。
     * @param className   类名
     * @param superClass  父类
     * @param listener    在回调里面写自己的方法体
     */
    public CreateClassImpl(int javaVersion, String className, Class<?> superClass, SimpleOnAddMethodListener listener) {
        mSuperClassName = superClass == null ? "java/lang/Object" : ASMUtil.getASMClassFullName(superClass);
        // 这里使用COMPUTE_FRAMES模式，就不用自己计算max_stack、local_stack和stack_map_table
        mClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        mClassWriter.visit(javaVersion, Opcodes.ACC_PUBLIC, className, null, mSuperClassName, null);
        mAddFieldImpl = new AddFieldImpl(mClassWriter);
        mAddMethodImpl = new AddMethodImpl(mClassWriter);
        mClassName = className;
        addConstructorMethod(Opcodes.ACC_PUBLIC, "()V", null, new SimpleOnAddMethodListener() {
            @Override
            public void onAddMethodBody(MethodVisitor mv) {
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, mSuperClassName, Const.CONSTRUCTOR_NAME, "()V", false);
                if (listener != null) {
                    listener.onAddMethodBody(mv);
                }
            }
        });
    }


    @Override
    public void addAnnotation(IAddAnnotation listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener is null");
        }
        ASMUtil.addAnnotations(mClassWriter, listener, null);
    }

    @Override
    public void addConstructorMethod(int access, String desc, SimpleOnAddMethodListener listener) {
        addConstructorMethod(access, desc, null, listener);
    }

    @Override
    public void addConstructorMethod(int access, String desc, Class<?>[] exceptions, SimpleOnAddMethodListener listener) {
        mAddMethodImpl.addConstructorMethod(access, desc, exceptions, new SimpleOnAddMethodListener() {
            @Override
            public void onAddMethodBody(MethodVisitor mv) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                if (listener != null) {
                    listener.onAddMethodBody(mv);
                }
            }
        });
    }

    @Override
    public void addMethod(int access, String name, String desc, SimpleOnAddMethodListener listener) {
        addMethod(access, name, desc, listener);
    }

    @Override
    public void addMethod(int access, String name, String desc, Class<?>[] exceptions, SimpleOnAddMethodListener listener) {
        mAddMethodImpl.addMethod(access, name, desc, exceptions, listener);
    }

    @Override
    public void addField(int access, String name, String desc, Object defaultValue, OnAddFiledListener listener) {
        mAddFieldImpl.addField(access, name, desc, defaultValue, listener);
    }

    @Override
    public void addField(int access, String name, String desc, Object defaultValue) {
        mAddFieldImpl.addField(access, name, desc, defaultValue, null);
    }

    @Override
    public byte[] toByteArray() {
        return mClassWriter.toByteArray();
    }


}
