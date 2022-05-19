package com.leilu.asm.gradle.thread_schedule;

import com.leilu.xasm.ASMUtil;
import com.leilu.xasm.base.impl.modify.bean.MethodInfo;
import com.leilu.xasm.base.inter.IHook;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * 思路：
 * 1、在需要hook的方法的onMethodStart回调之前创建一个带有同样签名和方法体的新方法
 * 2、在旧的方法里面调用代理类进行新创建的方法的调用
 */
public class HookMethodWithAnnotationListener implements IHook.OnHookMethodWithAnnotationListener {

    private ThreadSchedule mThreadSchedule;
    private String mNewMethodName;
    private String mProxyClassName;

    public HookMethodWithAnnotationListener(ThreadSchedule threadSchedule) {
        mThreadSchedule = threadSchedule;
    }


    @Override
    public InsnList onMethodStart(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
        // 在这里判断这个方法的是否同时带有BGThread和MainThread注解，如果有，则抛出异常
        if (methodInfo.annotationMap.containsKey(Const.DESC_METHOD_RUN_ON_BG_THREAD)
                && methodInfo.annotationMap.containsKey(Const.DESC_METHOD_RUN_ON_MAIN_THREAD)) {
            throw new ThreadSchedulePluginException("Only one BGThread and MainThread can exist at the same time !");
        }

        // 1、克隆一个带有相同方法体、签名、异常的方法
        cloneNewMethod(cn, mn);
        // 2、创建一个代理类
        createProxyClass(cn.name, mn);
        // 3、这里需要清空方法体，如果直接通过clear所有指令，在带有try catch语句块的方法
        // 的时候会报错，这里想了一个办法，就是先直接删除该方法，然后再创建一个和本方法同名的新方法
        removeMethodReCreate(cn, mn, methodInfo);
        return null;
    }


    @Override
    public boolean visitInsnMode(ClassNode cn, AbstractInsnNode abstractInsnNode, MethodInfo methodInfo) {
        return false;
    }

    @Override
    public InsnList onMethodEnd(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
        return null;
    }

    private void removeMethodReCreate(ClassNode cn, MethodNode sourceMethodNode, MethodInfo methodInfo) {
        // 移除方法
        cn.methods.remove(sourceMethodNode);
        // 创建一个相同的方法
        MethodNode newMethod = ASMUtil.cloneMethod(sourceMethodNode.access, sourceMethodNode.name, sourceMethodNode);

        cn.methods.add(newMethod);
    }

    private void cloneNewMethod(ClassNode cn, MethodNode sourceMethodNode) {
        // 创建一个同样方法体的新方法
        mNewMethodName = sourceMethodNode.name + "_" + sourceMethodNode.desc.replace("(", "")
                .replace(")", "")
                .replace(";", "");
        MethodNode newMethodNode = ASMUtil.cloneMethod(Opcodes.ACC_PUBLIC, mNewMethodName, sourceMethodNode);
        cn.methods.add(newMethodNode);
    }

    private void createProxyClass(String sourceClassName, MethodNode sourceMethod) {
        mProxyClassName = sourceClassName + "$" + mNewMethodName;
        byte[] proxyClassData = new HookMethodProxyCreator(sourceClassName, mNewMethodName
                , mProxyClassName, sourceMethod).create();
    }
}
