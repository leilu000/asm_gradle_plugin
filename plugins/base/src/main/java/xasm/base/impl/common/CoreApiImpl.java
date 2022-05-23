package xasm.base.impl.common;

import xasm.ASMUtil;
import xasm.base.Const;
import xasm.base.inter.ICoreApi;
import xasm.base.inter.INewClassListener;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * tree api的方式实现对象创建、方法调用等
 */
public class CoreApiImpl implements ICoreApi {

    @Override
    public void newCoreClass1(MethodVisitor mv, Class<?> clazz, Class<?>[] paramTypes, int[] paramSlotIndexes) {
        newCoreApiClass(mv, clazz, new INewClassListener() {

            @Override
            public void onTransmitParams() {
                accessCoreApiVar(mv, paramTypes, paramSlotIndexes);
            }

            @Override
            public Class<?>[] getParamTypes() {
                return paramTypes;
            }
        });
    }

    @Override
    public void newCoreClass2(MethodVisitor mv, Class<?> clazz, Object[] params) {
        newCoreApiClass(mv, clazz, new INewClassListener() {
            Class<?>[] paramTypes = null;

            @Override
            public void onTransmitParams() {
                transmitCoreApiParams(mv, paramTypes, params);
            }

            @Override
            public Class<?>[] getParamTypes() {
                return paramTypes;
            }
        });
    }

    @Override
    public void invokeCoreMethod1(MethodVisitor mv, Class<?> owner, String name, Class<?> returnType
            , Class<?>[] paramTypes, Object[] params, boolean isStatic) {
        transmitCoreApiParams(mv, paramTypes, params);
        int opcode = isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
        mv.visitMethodInsn(opcode, ASMUtil.getASMClassFullName(owner)
                , name, ASMUtil.getMethodDescriber(returnType, paramTypes), false);
    }

    @Override
    public void invokeCoreMethod2(MethodVisitor mv, Class<?> owner, String name, Class<?> returnType
            , Class<?>[] paramTypes, int[] paramSlotIndexes, boolean isStatic) {
        accessCoreApiVar(mv, paramTypes, paramSlotIndexes);
        int opcode = isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
        mv.visitMethodInsn(opcode, ASMUtil.getASMClassFullName(owner)
                , name, ASMUtil.getMethodDescriber(returnType, paramTypes), false);
    }

    @Override
    public void getCoreField(MethodVisitor mv, Class<?> owner, String name, Class<?> type, boolean isStatic) {
        int opcode = !isStatic ? Opcodes.GETFIELD : Opcodes.GETSTATIC;
        mv.visitFieldInsn(opcode, ASMUtil.getASMClassFullName(owner), name, ASMUtil.getDescriberByClass(type));
    }

    // core api的方式根据参数类型和局部变量表访问变量
    private void accessCoreApiVar(MethodVisitor mv, Class<?>[] paramTypes, int[] paramSlotIndexes) {
        if (paramTypes != null && paramSlotIndexes != null) {
            for (int i = 0; i < paramTypes.length; i++) {
                mv.visitVarInsn(ASMUtil.getLoadOpcode(paramTypes[i]), paramSlotIndexes[i]);
            }
        }
    }

    // core api方式传递参数
    private void transmitCoreApiParams(MethodVisitor mv, Class<?>[] paramTypes, Object[] params) {
        if (params != null && params.length > 0) {
            paramTypes = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].getClass();
                ASMUtil.transmitParam(mv, params[i]);
            }
        }
    }


    // 使用core api的方式new一个类，因为有一部分的代码是重复的，这里单独抽取一份方法，方便复用
    private void newCoreApiClass(MethodVisitor mv, Class<?> clazz, INewClassListener listener) {
        mv.visitTypeInsn(Opcodes.NEW, ASMUtil.getASMClassFullName(clazz));
        mv.visitInsn(Opcodes.DUP);
        listener.onTransmitParams();
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtil.getASMClassFullName(clazz),
                Const.CONSTRUCTOR_NAME,
                ASMUtil.getMethodDescriber(null, listener.getParamTypes()),
                false);
    }
}
