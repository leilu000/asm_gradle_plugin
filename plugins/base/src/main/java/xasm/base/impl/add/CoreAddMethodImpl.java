package xasm.base.impl.add;

import xasm.ASMUtil;
import xasm.base.impl.SimpleOnCoreAddMethodListener;
import xasm.XASM;
import xasm.base.Const;
import xasm.base.inter.IAddMethod;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


import java.util.ArrayList;
import java.util.List;

/**
 * 使用core api的方式添加方法的实现类
 */
public class CoreAddMethodImpl implements IAddMethod {

    private final ClassWriter mClassWriter;
    // 防止重复添加，将已经添加的数据存放到这个集合
    private final List<String> mDescriberList = new ArrayList<>();

    public CoreAddMethodImpl(ClassWriter classWriter) {
        mClassWriter = classWriter;
    }


    @Override
    public void addConstructorMethod(int access, String desc, SimpleOnCoreAddMethodListener listener) {
        addConstructorMethod(access, desc, null, listener);
    }

    @Override
    public void addConstructorMethod(int access, String desc, String[] exceptions, SimpleOnCoreAddMethodListener listener) {
        addObjectReturnMethod(access, Const.CONSTRUCTOR_NAME, desc, Opcodes.RETURN, exceptions, listener);
    }

    @Override
    public void addFields(int access, String varPrefix, Type[] types) {
        for (int i = 0; i < types.length; i++) {
            mClassWriter.visitField(access, varPrefix + i, types[i].getDescriptor(), null, null);
        }
    }

    @Override
    public void addMethod(int access, String name, String desc, SimpleOnCoreAddMethodListener listener) {
        addMethod(access, name, desc, null, listener);
    }

    @Override
    public void addMethod(int access, String name, String desc, String[] exceptions, SimpleOnCoreAddMethodListener listener) {
        if (Const.CONSTRUCTOR_NAME.equals(name)) {
            throw new IllegalArgumentException("Please use addConstructorMethod to add " + name + "  method !");
        }
        int opcodesReturn = ASMUtil.getOpcodecReturnValue(desc);
        addObjectReturnMethod(access, name, desc, opcodesReturn, exceptions, listener);
    }

    private void addObjectReturnMethod(int access, String name, String desc, int opcodesReturn, String[] exceptions
            , SimpleOnCoreAddMethodListener listener) {

        // 防止重复添加相同签名的方法
        if (mDescriberList.contains(name + desc)) {
            XASM.getInstance().getLogger().w("Please dont add same method,the method name is :" + name + "  and the " +
                    "describer is :" + desc);
            return;
        }
        mDescriberList.add(name + desc);

        MethodVisitor mv = mClassWriter.visitMethod(access, name, desc, null, exceptions);
        mv.visitCode();
        if (listener != null) {
            // 如果不是构造方法和静态代码块才允许添加注解
            if (!(Const.CONSTRUCTOR_NAME.equals(name) || Const.STATIC_BLOCK_NAME.equals(name))) {
                ASMUtil.addAnnotations(mv, listener, null);
            }
            // 回调过去添加方法体
            listener.onAddMethodBody(mClassWriter, mv);
        }
        mv.visitInsn(opcodesReturn);
        // 由于创建ClassWriter的时候使用的是COMPUTE_FRAMES模式，这个模式会
        // 自动帮计算maxStack、maxLocal和stack_map_Table，所以这里统一填1,
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

}
