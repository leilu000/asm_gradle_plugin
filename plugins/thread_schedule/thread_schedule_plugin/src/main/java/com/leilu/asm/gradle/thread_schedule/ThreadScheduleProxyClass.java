package com.leilu.asm.gradle.thread_schedule;


import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;

import xasm.ASMUtil;
import xasm.CreateClassWrapper;
import xasm.XASM;
import xasm.base.impl.SimpleOnCoreAddMethodListener;
import xasm.base.impl.modify.bean.MethodInfo;

public class ThreadScheduleProxyClass {

    private static final String VAR_PREFIX = "mVar";
    private static final String METHOD_NAME_INIT = "init";

    private MethodInfo mMethodInfo;
    private String mPackageName;
    private String mProxyClassName;
    private String mSourceClassName;
    private String mInitMethodDesc;
    private Type[] mParamTypes;

    private int mStoreProxyInstanceIndex;
    private int mStoreResultIndex;

    public ThreadScheduleProxyClass(String sourceClassName, MethodInfo methodInfo) {
        mSourceClassName = sourceClassName;
        mMethodInfo = methodInfo;
        mPackageName = sourceClassName.substring(0, sourceClassName.lastIndexOf("/"));
        mProxyClassName = mPackageName + "/" + "ProxyClass_" + mMethodInfo.name + "_" + System.currentTimeMillis();
        String desc = methodInfo.desc;
        if (methodInfo.isStatic) {
            mInitMethodDesc = desc.substring(0, desc.lastIndexOf(")") + 1) + "V";
        } else {
            String destStr = desc.substring(0, desc.lastIndexOf(")"))
                    .replace("(", "")
                    .replace(")", "");
            mInitMethodDesc = "(L" + sourceClassName + ";" + destStr + ")V";
        }
        mParamTypes = Type.getArgumentTypes(mInitMethodDesc);

        mStoreProxyInstanceIndex = ASMUtil.getLastNextMethodParamIndex(mInitMethodDesc);
    }

    public int getProxyInstanceStoreIndex() {
        return mStoreProxyInstanceIndex;
    }

    public String getProxyClassName() {
        return mProxyClassName;
    }

    /**
     * 创建代理类
     *
     * @return
     */
    public ProxyClassInfo create() {
        ProxyClassInfo info = new ProxyClassInfo();
        info.instance = this;
        info.className = mProxyClassName;
        info.simpleClassName = mProxyClassName.substring(mProxyClassName.lastIndexOf("/") + 1);
        CreateClassWrapper wrapper = XASM.getInstance()
                .createClass(mProxyClassName, null, new String[]{"java/lang/Runnable"}, null)
                .addMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, mRunMethodListener)
                .addMethod(Opcodes.ACC_PUBLIC, METHOD_NAME_INIT, mInitMethodDesc, null, mInitMethodListener)
                .addFields(Opcodes.ACC_PRIVATE, VAR_PREFIX, mInitMethodDesc);
        // 判断被hook的方法是否有返回值，如果有返回值，则需要额外创建一个设置阻塞器的方法方便调用和获取异步运行的返回值
        if (mMethodInfo.hasReturnValue) {
            // 添加setCountDownLatch方法
            wrapper.addMethod(Opcodes.ACC_PUBLIC
                    , Const.METHOD_SET_COUNT_DOWN_LATCH
                    , Const.DESC_METHOD_SET_COUNT_DOWN_LATCH
                    , null
                    , mSetCountDownLatchMethodListener);
            // 添加countDownLatch属性
            wrapper.addField(Opcodes.ACC_PRIVATE
                    , Const.FIELD_COUNT_DOWN_LATCH
                    , Const.DESC_COUNT_DOWN_LATCH
                    , null);
            // 添加接收对应返回值的属性
            wrapper.addField(Opcodes.ACC_PUBLIC
                    , Const.FIELD_RESULT
                    , mMethodInfo.returnDesc
                    , null);

        }
        info.data = wrapper.toByteArray();
        return info;
    }

    /**
     * 实例化代理类对象
     *
     * @return
     */
    public InsnList newProxyInstance() {
        InsnList list = new InsnList();
        list.add(new TypeInsnNode(Opcodes.NEW, mProxyClassName));
        list.add(new InsnNode(Opcodes.DUP));
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, mProxyClassName, "<init>", "()V", false));
        // 保存proxy实例
        list.add(new VarInsnNode(Opcodes.ASTORE, mStoreProxyInstanceIndex));
        return list;
    }

    /**
     * 调用代理类的init方法
     *
     * @return
     */
    public InsnList invokeInitMethod() {
        InsnList list = new InsnList();
        boolean isStatic = mMethodInfo.isStatic;
        // 加载proxy实例
        list.add(loadProxyInstance());
        if (!isStatic) {
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }
        int start = isStatic ? 0 : 1;
        int index = start;
        for (int i = start; i < mParamTypes.length; i++) {
            Type type = mParamTypes[i];
            list.add(new VarInsnNode(ASMUtil.getLoadOpcodec(type.getDescriptor()), index));
            index += type.getSize();
        }
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, mProxyClassName, METHOD_NAME_INIT, mInitMethodDesc, false));
        return list;
    }

    /**
     * 加载对象实例
     *
     * @return
     */
    public InsnList loadProxyInstance() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, mStoreProxyInstanceIndex));
        return list;
    }

    /**
     * 加载返回值
     *
     * @return
     */
    public InsnList loadCountDownLatchField() {
        InsnList list = new InsnList();
        list.add(loadProxyInstance());
        list.add(new FieldInsnNode(Opcodes.GETFIELD, mProxyClassName, Const.FIELD_RESULT, mMethodInfo.returnDesc));
        return list;
    }

    private final SimpleOnCoreAddMethodListener mSetCountDownLatchMethodListener = new SimpleOnCoreAddMethodListener() {

        @Override
        public void onAddMethodBody(ClassWriter cw, MethodVisitor mv) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD
                    , mProxyClassName,
                    Const.FIELD_COUNT_DOWN_LATCH,
                    Const.DESC_COUNT_DOWN_LATCH);
        }
    };

    /**
     * 创建run方法的方法体
     */
    private final SimpleOnCoreAddMethodListener mRunMethodListener = new SimpleOnCoreAddMethodListener() {
        @Override
        public void onAddMethodBody(ClassWriter cw, MethodVisitor mv) {
            boolean isStatic = mMethodInfo.isStatic;
            if (!isStatic) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, mProxyClassName, VAR_PREFIX + 0, mParamTypes[0].getDescriptor());
            }
            int start = isStatic ? 0 : 1;
            for (int i = start; i < mParamTypes.length; i++) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, mProxyClassName, VAR_PREFIX + i, mParamTypes[i].getDescriptor());
            }
            int opcode = isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
            mv.visitMethodInsn(opcode, mSourceClassName, mMethodInfo.name, mMethodInfo.desc, false);
            // 如果有返回值，给返回值赋值，并唤醒阻塞线程
            if (mMethodInfo.hasReturnValue) {
                mStoreResultIndex = mStoreProxyInstanceIndex + 1;
                mv.visitVarInsn(ASMUtil.getStoreOpcodec(mMethodInfo.returnDesc), mStoreResultIndex);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(ASMUtil.getLoadOpcodec(mMethodInfo.returnDesc), mStoreResultIndex);
                mv.visitFieldInsn(Opcodes.PUTFIELD
                        , mProxyClassName
                        , Const.FIELD_RESULT
                        , mMethodInfo.returnDesc);

                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD
                        , mProxyClassName
                        , Const.FIELD_COUNT_DOWN_LATCH
                        , Const.DESC_COUNT_DOWN_LATCH);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL
                        , Const.CLASS_NAME_TRY_CATCH_COUNTDOWN_LATCH
                        , Const.METHOD_COUNT_DOWN
                        , Const.DESC_METHOD_COUNT_DOWN_LATCH
                        , false);
            }
        }
    };

    /**
     * 创建init方法的方法体
     */
    private final SimpleOnCoreAddMethodListener mInitMethodListener = new SimpleOnCoreAddMethodListener() {


        @Override
        public void onAddMethodBody(ClassWriter cw, MethodVisitor mv) {
            int index = 1;
            for (int i = 0; i < mParamTypes.length; i++) {
                Type type = mParamTypes[i];
                String desc = type.getDescriptor();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(ASMUtil.getLoadOpcodec(desc), index);
                mv.visitFieldInsn(Opcodes.PUTFIELD, mProxyClassName, VAR_PREFIX + i, desc);
                index += type.getSize();
            }
        }

    };


    public static class ProxyClassInfo {
        public byte[] data;
        public String className;
        public String simpleClassName;
        ThreadScheduleProxyClass instance;
    }
}
