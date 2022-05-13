package com.leilu.asm.method_hook;

import com.android.build.api.transform.Status;
import com.leilu.base.BasePlugin;
import com.leilu.xasm.ASMUtil;
import com.leilu.xasm.XASM;
import com.leilu.xasm.base.impl.modify.bean.MethodInfo;
import com.leilu.xasm.base.inter.IHook;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.PrintStream;

public class MethodHookPlugin extends BasePlugin<MethodHookConfig> {

    @Override
    protected byte[] modifyClass(byte[] classData, Status status) {
        return XASM.getInstance()
                .modifyClass(classData)
                .hookMethod("onClick"
                        , "(Landroid/view/View;)V"
                        , "android/view/View$OnClickListener"
                        , new IHook.OnHookMethodListener() {
                            @Override
                            public InsnList onMethodStart(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
                                return createInsnList(mn.name + "方法开始");
                            }

                            @Override
                            public boolean visitInsnNode(ClassNode cn, AbstractInsnNode abstractInsnNode, MethodInfo methodInfo) {
                                return false;
                            }

                            @Override
                            public InsnList onMethodEnd(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
                                return createInsnList(mn.name + "方法结束");
                            }
                        })
                .toByteArray();
    }

    private InsnList createInsnList(String msg) {
        InsnList list = new InsnList();
        list.add(new FieldInsnNode(Opcodes.GETSTATIC
                , ASMUtil.getASMClassFullName(System.class)
                , "out"
                , ASMUtil.getDescriberByClass(PrintStream.class)));
        list.add(new LdcInsnNode(msg));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL
                , ASMUtil.getASMClassFullName(PrintStream.class)
                , "println"
                , ASMUtil.getMethodDescriber(null, new Class[]{String.class})));
        return list;
    }

    @Override
    protected MethodHookConfig initSelfDefineExtension() {
        return new MethodHookConfig();
    }

}
