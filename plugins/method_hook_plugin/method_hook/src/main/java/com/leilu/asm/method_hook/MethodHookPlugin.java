package com.leilu.asm.method_hook;

import com.android.build.api.transform.Status;
import com.leilu.base.BasePlugin;

import xasm.ASMUtil;
import xasm.XASM;
import xasm.base.impl.modify.bean.MethodInfo;
import xasm.base.inter.IHook;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.PrintStream;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class MethodHookPlugin extends BasePlugin<MethodHookConfig> {

    @Override
    protected byte[] modifyJarInputClass(String className, JarOutputStream jos, byte[] sourceData, Status status) {
        return modifyClass(className, null, jos, sourceData, status);
    }

    @Override
    protected byte[] modifyDirectorInputClass(String className, String destDir, byte[] sourceData, Status status) {
        return modifyClass(className, destDir, null, sourceData, status);
    }

    private byte[] modifyClass(String className, String descDir, JarOutputStream jos, byte[] sourceData, Status status) {
        return XASM.getInstance()
                .modifyClass(sourceData)
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
                .toByteArray().data;

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
