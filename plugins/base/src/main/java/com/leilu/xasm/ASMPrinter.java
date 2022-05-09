package com.leilu.xasm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * 用来添加System.out.println语句的工具类
 */
public class ASMPrinter {


    /**
     * 打印println格式
     *
     * @param methodNode
     * @param msg
     */
    public void println(MethodNode methodNode, int msg) {
        printlin(methodNode, msg, "println");
    }

    /**
     * 打印println格式
     *
     * @param methodNode
     * @param msg
     */
    public void println(MethodNode methodNode, byte msg) {
        printlin(methodNode, msg, "println");
    }

    /**
     * 打印println格式
     *
     * @param methodNode
     * @param msg
     */
    public void println(MethodNode methodNode, short msg) {
        printlin(methodNode, msg, "println");
    }

    /**
     * 打印println格式
     *
     * @param methodNode
     * @param msg
     */
    public void println(MethodNode methodNode, char msg) {
        printlin(methodNode, msg, "println");
    }

    /**
     * 打印println格式
     *
     * @param methodNode
     * @param msg
     */
    public void println(MethodNode methodNode, float msg) {
        printlin(methodNode, msg, "println");
    }

    /**
     * 打印println格式
     *
     * @param methodNode
     * @param msg
     */
    public void println(MethodNode methodNode, double msg) {
        printlin(methodNode, msg, "println");
    }

    /**
     * 打印println格式
     *
     * @param methodNode
     * @param msg
     */
    public void println(MethodNode methodNode, long msg) {
        printlin(methodNode, msg, "println");
    }

    /**
     * 打印println格式
     *
     * @param methodNode
     * @param msg
     */
    public void println(MethodNode methodNode, boolean msg) {
        printlin(methodNode, msg, "println");
    }

    /**
     * 打印println格式
     *
     * @param methodNode
     * @param msg
     */
    public void println(MethodNode methodNode, String msg) {
        printlin(methodNode, msg, "println");
    }

    /**
     * 打印print格式
     *
     * @param mv
     * @param msg
     */
    public void println(MethodVisitor mv, int msg) {
        println(mv, msg, "println");
    }

    /**
     * 打印print格式
     *
     * @param mv
     * @param msg
     */
    public void println(MethodVisitor mv, char msg) {
        println(mv, msg, "println");
    }

    /**
     * 打印print格式
     *
     * @param mv
     * @param msg
     */
    public void println(MethodVisitor mv, float msg) {
        println(mv, msg, "println");
    }

    /**
     * 打印print格式
     *
     * @param mv
     * @param msg
     */
    public void println(MethodVisitor mv, double msg) {
        println(mv, msg, "println");
    }

    /**
     * 打印print格式
     *
     * @param mv
     * @param msg
     */
    public void println(MethodVisitor mv, short msg) {
        println(mv, msg, "println");
    }

    /**
     * 打印print格式
     *
     * @param mv
     * @param msg
     */
    public void println(MethodVisitor mv, long msg) {
        println(mv, msg, "println");
    }

    /**
     * 打印print格式
     *
     * @param mv
     * @param msg
     */
    public void println(MethodVisitor mv, String msg) {
        println(mv, msg, "println");
    }

    /**
     * 打印print格式
     *
     * @param mv
     * @param msg
     */
    public void println(MethodVisitor mv, byte msg) {
        println(mv, msg, "println");
    }

    /**
     * 打印print格式
     *
     * @param mv
     * @param msg
     */
    public void println(MethodVisitor mv, boolean msg) {
        println(mv, msg, "println");
    }

    private void println(MethodVisitor mv, Object msg, String methodName) {
        mv.visitFieldInsn(Opcodes.GETSTATIC
                , "java/lang/System"
                , "out"
                , "Ljava/io/PrintStream;");
        mv.visitLdcInsn(msg);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL
                , "java/io/PrintStream"
                , methodName
                , getMethodDescriber(msg)
                , false);
    }


    private void printlin(MethodNode methodNode, Object msg, String methodName) {
        InsnList insnList = methodNode.instructions;
        insnList.add(new FieldInsnNode(Opcodes.GETSTATIC
                , "java/lang/System"
                , "out"
                , "Ljava/io/PrintStream;")
        );
        insnList.add(new LdcInsnNode(msg));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL
                , "java/io/PrintStream"
                , methodName
                , getMethodDescriber(msg)
        ));
    }

    private String getMethodDescriber(Object obj) {
        return "(" + ASMUtil.getDescriberByObject(obj) + ")V";
    }

}
