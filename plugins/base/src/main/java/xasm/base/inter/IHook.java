package xasm.base.inter;

import xasm.base.impl.modify.bean.MethodInfo;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public interface IHook {

    /**
     * hook方法的带有某些注解的方法回调
     */
    interface OnHookMethodWithAnnotationListener {
        /**
         * 在方法开始前插入代码
         *
         * @param cn
         * @param mn
         * @param methodInfo
         * @return 插入的指令集合，如果为空，则不插入任何方法
         */
        InsnList onMethodStart(ClassNode cn, MethodNode mn, MethodInfo methodInfo);

        /**
         * 遍历方法体的指令，可以在里面替换类的创建，方法的调用等
         *
         * @param cn
         * @param abstractInsnNode
         * @param methodInfo
         * @return 是否删除该指令，如果返回true，则删除该指令；false，则不删除该指令
         */
        boolean visitInsnMode(ClassNode cn, AbstractInsnNode abstractInsnNode, MethodInfo methodInfo);

        /**
         * 在方法结束后插入代码
         *
         * @param cn
         * @param mn
         * @param methodInfo
         * @return 插入的指令集合，如果为空，则不插入任何方法
         */
        InsnList onMethodEnd(ClassNode cn, MethodNode mn, MethodInfo methodInfo);
    }

    /**
     * hook方法的回调
     */
    interface OnHookMethodListener {
        /**
         * 在方法开始前插入代码
         *
         * @param cn
         * @param mn
         * @param methodInfo
         * @return 插入的指令集合，如果为空，则不插入任何方法
         */
        InsnList onMethodStart(ClassNode cn, MethodNode mn, MethodInfo methodInfo);

        /**
         * 遍历方法体的指令，可以在里面替换类的创建，方法的调用等
         *
         * @param cn
         * @param abstractInsnNode
         * @param methodInfo
         * @return 是否删除该指令，如果返回true，则删除该指令；false，则不删除该指令
         */
        boolean visitInsnNode(ClassNode cn, AbstractInsnNode abstractInsnNode, MethodInfo methodInfo);

        /**
         * 在方法结束后插入代码
         *
         * @param cn
         * @param mn
         * @param methodInfo
         * @return 插入的指令集合，如果为空，则不插入任何方法
         */
        InsnList onMethodEnd(ClassNode cn, MethodNode mn, MethodInfo methodInfo);
    }

    /**
     * hook某个方法
     *
     * @param methodName     方法名
     * @param returnType     返回值类型，没有返回值则为null
     * @param parameterTypes 方法参数，没有参数则为null
     * @param listener       hook方法的回调
     */
    void hookMethod(String methodName, Class<?> returnType, Class<?>[] parameterTypes, OnHookMethodListener listener);

    /**
     * hook某个方法，不兼容lambda表达式，如果需要hook lambda表达式，则使用 调用interfaceName参数的hookMethod方法
     *
     * @param methodName 方法名
     * @param describer  方法签名
     * @param listener   hook方法的回调
     */
    void hookMethod(String methodName, String describer, OnHookMethodListener listener);

    /**
     * hook某个方法
     *
     * @param methodName    方法名
     * @param describer     方法签名
     * @param interfaceName 方法所在接口，如果传null或者""，则不会hook lambda表达式的方法
     *                      需要填全类名，比如：java/langRunnable、android/view$OnClickListener等
     * @param listener      hook方法的回调
     */
    void hookMethod(String methodName, String describer, String interfaceName, OnHookMethodListener listener);

    /**
     * hook带有某些annotation的方法
     *
     * @param annotationDescriber
     * @param listener
     */
    void hookMethodWithAnnotations(List<String> annotationDescriber, OnHookMethodWithAnnotationListener listener);

    /**
     * 开始hook操作
     *
     * @param sourceData
     * @return hook以后的内容
     */
    byte[] startHook(byte[] sourceData);


}
