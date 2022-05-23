package xasm.base.impl.common;

import xasm.ASMUtil;
import xasm.base.Const;
import xasm.base.inter.INewClassListener;
import xasm.base.inter.ITreeApi;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * tree api的方式实现对象创建、方法调用等
 */
public class TreeApiImpl implements ITreeApi {


    @Override
    public InsnList newTreeClass1(Class<?> clazz, Class<?>[] paramTypes, int[] paramSlotIndexes) {
        ASMUtil.checkParamType(paramTypes, paramSlotIndexes);
        InsnList list = new InsnList();
        newTreeApiClass(list, clazz, new INewClassListener() {
            @Override
            public void onTransmitParams() {
                accessTreeApiVar(list, paramTypes, paramSlotIndexes);
            }

            @Override
            public Class<?>[] getParamTypes() {
                return paramTypes;
            }
        });
        return list;
    }

    @Override
    public InsnList newTreeClass2(Class<?> clazz, Class<?>[] paramTypes, Object[] params) {
        InsnList list = new InsnList();
        newTreeApiClass(list, clazz, new INewClassListener() {
            @Override
            public void onTransmitParams() {
                transmitTreeApiParams(list, paramTypes, params);
            }

            @Override
            public Class<?>[] getParamTypes() {
                return paramTypes;
            }
        });
        return list;
    }

    @Override
    public InsnList invokeTreeMethod1(Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes, Object[] params, boolean isStatic) {
        InsnList list = new InsnList();
        transmitTreeApiParams(list, paramTypes, params);
        int opcode = isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
        list.add(new MethodInsnNode(opcode, ASMUtil.getASMClassFullName(owner)
                , name, ASMUtil.getMethodDescriber(returnType, paramTypes)));
        return list;
    }

    @Override
    public InsnList invokeTreeMethod2(Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes, int[] paramSlotIndexes, boolean isStatic) {
        ASMUtil.checkParamType(paramTypes, paramSlotIndexes);
        InsnList list = new InsnList();
        accessTreeApiVar(list, paramTypes, paramSlotIndexes);
        int opcode = isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
        list.add(new MethodInsnNode(opcode, ASMUtil.getASMClassFullName(owner)
                , name, ASMUtil.getMethodDescriber(returnType, paramTypes)));
        return list;
    }

    @Override
    public InsnList getTreeField(Class<?> owner, String name, Class<?> type, boolean isStatic) {
        InsnList list = new InsnList();
        int opcode = isStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
        list.add(new FieldInsnNode(opcode, ASMUtil.getASMClassFullName(owner), name, ASMUtil.getDescriberByClass(type)));
        return list;
    }

    // tree api的方式根据参数类型和局部变量表访问变量
    private void accessTreeApiVar(InsnList list, Class<?>[] paramTypes, int[] paramSlotIndexes) {
        if (paramTypes != null && paramSlotIndexes != null) {
            for (int i = 0; i < paramTypes.length; i++) {
                list.add(new VarInsnNode(ASMUtil.getLoadOpcode(paramTypes[i]), paramSlotIndexes[i]));
            }
        }
    }


    // tree api方式传递参数
    private void transmitTreeApiParams(InsnList list, Class<?>[] paramTypes, Object[] params) {
        if (params != null && params.length > 0) {
            paramTypes = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].getClass();
                list.add(ASMUtil.transmitParam(params[i]));
            }
        }
    }

    // 使用tree api的方式new一个类，因为有一部分的代码是重复的，这里单独抽取一份方法，方便复用
    private void newTreeApiClass(InsnList list, Class<?> clazz, INewClassListener listener) {
        list.add(new TypeInsnNode(Opcodes.NEW, ASMUtil.getASMClassFullName(clazz)));
        list.add(new org.objectweb.asm.tree.InsnNode(Opcodes.DUP));

        listener.onTransmitParams();

        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL
                , ASMUtil.getASMClassFullName(clazz)
                , Const.CONSTRUCTOR_NAME
                , ASMUtil.getMethodDescriber(null, listener.getParamTypes())
                , false));
    }
}
