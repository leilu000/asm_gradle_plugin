package xasm.base.impl;

import xasm.base.impl.common.CoreApiImpl;
import xasm.base.impl.common.TreeApiImpl;
import xasm.base.inter.IASMHelper;
import xasm.base.inter.ICoreApi;
import xasm.base.inter.ITreeApi;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;

/**
 * ASM方法调用、对象创建等的帮助类，基于core api和tree api两种方式来实现
 */
public class ASMHelperImpl implements IASMHelper {

    private final ITreeApi mTreeApi = new TreeApiImpl();
    private final ICoreApi mCoreApi = new CoreApiImpl();

    @Override
    public void newCoreClass1(MethodVisitor mv, Class<?> clazz, Class<?>[] paramTypes, int[] paramSlotIndexes) {
        mCoreApi.newCoreClass1(mv, clazz, paramTypes, paramSlotIndexes);
    }

    @Override
    public void newCoreClass2(MethodVisitor mv, Class<?> clazz, Object[] params) {
        mCoreApi.newCoreClass2(mv, clazz, params);
    }

    @Override
    public InsnList newTreeClass1(Class<?> clazz, Class<?>[] paramTypes, int[] paramSlotIndexes) {
        return mTreeApi.newTreeClass1(clazz, paramTypes, paramSlotIndexes);
    }

    @Override
    public InsnList newTreeClass2(Class<?> clazz, Class<?>[] paramTypes, Object[] params) {
        return mTreeApi.newTreeClass2(clazz, paramTypes, params);
    }

    @Override
    public void invokeCoreMethod1(MethodVisitor mv, Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes, Object[] params, boolean isStatic) {
        mCoreApi.invokeCoreMethod1(mv, owner, name, returnType, paramTypes, params, isStatic);
    }

    @Override
    public void invokeCoreMethod2(MethodVisitor mv, Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes, int[] paramSlotIndexes, boolean isStatic) {
        mCoreApi.invokeCoreMethod2(mv, owner, name, returnType, paramTypes, paramSlotIndexes, isStatic);
    }

    @Override
    public void getCoreField(MethodVisitor mv, Class<?> owner, String name, Class<?> type, boolean isStatic) {
        mCoreApi.getCoreField(mv, owner, name, type, isStatic);
    }

    @Override
    public InsnList invokeTreeMethod1(Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes, Object[] params, boolean isStatic) {
        return mTreeApi.invokeTreeMethod1(owner, name, returnType, paramTypes, params, isStatic);
    }

    @Override
    public InsnList invokeTreeMethod2(Class<?> owner, String name, Class<?> returnType, Class<?>[] paramTypes, int[] paramSlotIndexes, boolean isStatic) {
        return mTreeApi.invokeTreeMethod2(owner, name, returnType, paramTypes, paramSlotIndexes, isStatic);
    }

    @Override
    public InsnList getTreeField(Class<?> owner, String name, Class<?> type, boolean isStatic) {
        return mTreeApi.getTreeField(owner, name, type, isStatic);
    }
}
