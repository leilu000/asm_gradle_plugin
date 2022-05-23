package xasm.base.inter;


import xasm.base.impl.SimpleOnCoreAddMethodListener;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * 和添加方法相关的接口
 */
public interface IAddMethod {

    /**
     * core api模式 创建方法的回调
     */
    interface OnCoreAddMethodListener extends IAddAnnotation {
        /**
         * 添加具体的方法内容，已经处理好方法的开始和结束，只需要添加对应的方法体内容即可
         * 无需再调用visitCode
         * 无需再调用visitInsn(Opcodec.XXRETURN)
         * 无需再调用visitMaxs方法
         *
         * @param cw
         * @param mv
         */
        void onAddMethodBody(ClassWriter cw, MethodVisitor mv);


    }

    /**
     * tree api模式 创建方法的回调
     */
    interface OnTreeAddMethodListener extends IAddAnnotation {

        /**
         * 添加具体的方法内容，已经处理好方法的开始和结束，只需要添加对应的方法体内容即可
         * 无需添加 Opcodec.XXRETURN
         *
         * @param cw
         * @param mv
         */
        void onAddMethodBody(ClassNode cn, MethodNode mn);
    }

    /**
     * @param access
     * @param varPrefix
     * @param types
     */
    void addFields(int access, String varPrefix, Type[] types);

    /**
     * 添加构造方法
     *
     * @param access   Opcodec.ACC_PUBLIC...
     * @param desc     方法签名
     * @param listener
     */
    void addConstructorMethod(int access, String desc, SimpleOnCoreAddMethodListener listener);

    /**
     * 添加构造方法
     *
     * @param access     Opcodec.ACC_PUBLIC...
     * @param desc       方法签名
     * @param exceptions 异常集合
     * @param listener
     */
    void addConstructorMethod(int access, String desc, String[] exceptions, SimpleOnCoreAddMethodListener listener);

    /**
     * 添加方法，不带抛出异常
     *
     * @param access Opcodec.ACC_PUBLIC...
     * @param name   方法名
     * @param desc   方法签名
     */
    void addMethod(int access, String name, String desc, SimpleOnCoreAddMethodListener listener);

    /**
     * 添加方法，带有抛出异常
     *
     * @param access     Opcodec.ACC_PUBLIC...
     * @param name       方法名
     * @param desc       方法签名
     * @param exceptions 异常数组
     */
    void addMethod(int access, String name, String desc, String[] exceptions, SimpleOnCoreAddMethodListener listener);

}
