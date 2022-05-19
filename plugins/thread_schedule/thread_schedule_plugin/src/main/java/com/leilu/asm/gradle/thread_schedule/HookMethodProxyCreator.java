package com.leilu.asm.gradle.thread_schedule;

import com.leilu.xasm.ASMUtil;
import com.leilu.xasm.XASM;
import com.leilu.xasm.base.impl.SimpleOnAddMethodListener;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public class HookMethodProxyCreator {

    private static final String SOURCE_OBJECT_NAME = "sourceObj";
    private static final String VAR_PREFFIX = "var";

    private String mSourceClassName;
    private String mProxyClassName;
    private String mCloneMethodName;
    private MethodNode mSourceMethod;
    private Type[] mMethodParamTypes;

    /**
     * @param sourceClassName 源类名
     * @param cloneMethodName 对应方法的克隆方法名
     * @param proxyClassName
     * @param sourceMethod
     */
    public HookMethodProxyCreator(String sourceClassName, String cloneMethodName
            , String proxyClassName, MethodNode sourceMethod) {
        mSourceClassName = sourceClassName;
        mCloneMethodName = cloneMethodName;
        mProxyClassName = proxyClassName;
        mSourceMethod = sourceMethod;
        mMethodParamTypes = Type.getArgumentTypes(sourceMethod.desc);
    }

    public byte[] create() {
        return XASM.getInstance()
                .createClass(mProxyClassName, null, new String[]{"java/lang/Runnable"}, null)
                .addFileds(Opcodes.ACC_PRIVATE, VAR_PREFFIX, mMethodParamTypes)
                .addMethod(Opcodes.ACC_PUBLIC, "run", "()V", new SimpleOnAddMethodListener() {
                    @Override
                    public void onAddMethodBody(ClassWriter cw, MethodVisitor mv) {
                        createRunMethodBody(mv);
                    }
                })
                .addMethod(Opcodes.ACC_PUBLIC, "init", mSourceMethod.desc, new SimpleOnAddMethodListener() {
                    @Override
                    public void onAddMethodBody(ClassWriter cw, MethodVisitor mv) {
                        createInitMethodBody(cw, mv);
                    }
                })
                .toByteArray();
    }

    // 创建init方法的方法体
    private void createInitMethodBody(ClassWriter cw, MethodVisitor mv) {
        // 给属性赋值
        mv.visitFieldInsn(Opcodes.ACC_PRIVATE, mProxyClassName, SOURCE_OBJECT_NAME, mSourceClassName);
        Type[] paramTypes = Type.getArgumentTypes(mSourceMethod.desc);
        if (paramTypes != null && paramTypes.length > 0) {
            for (int i = 0; i < paramTypes.length; i++) {
                Type param = paramTypes[i];
                String desc = param.getDescriptor();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(ASMUtil.getLoadOpcodec(desc), i + 1);
                mv.visitFieldInsn(Opcodes.PUTFIELD, mProxyClassName, VAR_PREFFIX + i, desc);
            }
        }
    }

    // 创建run方法的方法体
    private void createRunMethodBody(MethodVisitor mv) {
        mv.visitFieldInsn(Opcodes.ALOAD, mProxyClassName, SOURCE_OBJECT_NAME, mSourceClassName);
        ASMUtil.loadAllVar(mSourceMethod.access, mv, mSourceMethod.desc);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, mSourceClassName, mCloneMethodName, mSourceMethod.desc, false);
    }


}
