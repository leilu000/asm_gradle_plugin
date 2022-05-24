package com.leilu.asm.gradle.thread_schedule;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.awt.SystemTray;
import java.io.File;
import java.util.Map;

import xasm.ASMUtil;
import xasm.base.impl.modify.bean.MethodInfo;
import xasm.base.inter.IAddMethod;
import xasm.base.inter.IHook;

public class ThreadScheduleHooker implements IHook.OnHookMethodWithAnnotationListener {

    private ThreadSchedule mThreadSchedule;
    private AnnotationInfo mAnnotationInfo;
    // 当前被hook的类名
    private String mClassName;
    private int mTryCountDownLatchInstanceStoreIndex;
    private String mDestDirPath;

    public ThreadScheduleHooker(String destDir, ThreadSchedule schedule) {
        mThreadSchedule = schedule;
        mDestDirPath = destDir;
    }

    private AnnotationInfo getAnnotationInfo(String key, Map<String, Map<String, Object>> annotationMap) {
        if (annotationMap.containsKey(key)) {
            Map<String, Object> map = annotationMap.get(key);
            AnnotationInfo info = new AnnotationInfo();
            if (map.containsKey(Const.ANNOTATION_KEY_DELAY)) {
                info.delay = (long) map.get(Const.ANNOTATION_KEY_DELAY);
            }
            info.desc = key;
            return info;
        }
        return null;
    }

    @Override
    public InsnList onMethodStart(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
        mClassName = cn.name;
        // 为了防止hook的匿名内部类生成的class对象类型错误
        // 但本菜鸡还有没有搞清楚ASM是怎么生成匿名内部类的，所以这里通过取巧的方法获取匿名内部类的外部类
        mClassName = mClassName.substring(0, mClassName.indexOf("$"));

        AnnotationInfo bgThread = getAnnotationInfo(Const.CLASS_NAME_BG_THREAD, methodInfo.annotationMap);
        AnnotationInfo mainThread = getAnnotationInfo(Const.CLASS_NAME_MAIN_THREAD, methodInfo.annotationMap);
        // 如果方法同时出现BGThread和MainThread两个注解，则抛出异常
        if (bgThread != null && mainThread != null) {
            throw new ThreadSchedulePluginException("The " + mn.name + " method of " + mClassName
                    + " have BgThread and MainThread annotation at the same time !");
        }

        mAnnotationInfo = bgThread != null ? bgThread : mainThread;
        // 创建一个带有相同签名和方法体的新的方法
        MethodInfo newMethodInfo = createNewMethod(cn, mn);
        // 创建一个根据当前方法的参数等属性的代理类
        ThreadScheduleProxyClass.ProxyClassInfo proxyClass = createMethodProxyClass(mClassName, newMethodInfo);
//        // 清空当前方法，并创建一个新的方法体
        clearMethodAndCreateNewBody(proxyClass, cn, newMethodInfo, mn);
        return null;
    }

    /**
     * 删除当前方法，并创建一个一模一样的，然后再在新方法中写入线程调度逻辑
     *
     * @param proxyClassInfo
     * @param cn
     * @param newMethodInfo
     * @param mn
     */
    private void clearMethodAndCreateNewBody(ThreadScheduleProxyClass.ProxyClassInfo proxyClassInfo, ClassNode cn
            , MethodInfo newMethodInfo, MethodNode mn) {
        // 保存生成的代理类
        String proxyClassPath = mDestDirPath + File.separator + proxyClassInfo.simpleClassName + ".class";
        ASMUtil.saveClassData(proxyClassInfo.data, proxyClassPath);
        // 删除原来的方法
        cn.methods.remove(mn);
        // 重新创建一个同名同签名的方法
        ASMUtil.createMethodByTreeApi(cn, mn.access, mn.name, mn.desc, ASMUtil.stringListToArray(mn.exceptions)
                , new IAddMethod.OnTreeAddMethodListener() {
                    @Override
                    public void onAddMethodBody(ClassNode cn, MethodNode mn) {
                        addMethodBody(mn
                                , Const.CLASS_NAME_MAIN_THREAD.equals(mAnnotationInfo.desc)
                                , newMethodInfo
                                , proxyClassInfo);
                    }

                    @Override
                    public Map<String, Map<String, Object>> getAnnotationsKeyValue() {
                        return null;
                    }
                });
    }

    /**
     * 添加被hook方法的新方法体
     * <p>
     * 1、如果该注解是MainThread，则判断是否当前是否处于主线程，如果是，则直接调用newMethodInfo方法；
     * 如果不是，则生成代理类，通过ThreadScheduleUtil来调用
     * 2、如果该注解是BGThread，则判断是否当前是否处于非主线程，如果不是，则直接调用newMethodInfo方法；
     * 如果不是，则生成代理类，通过ThreadScheduleUtil来调用
     *
     * @param mn
     * @param isRunOnMainThread 是否是主线程调用
     * @param newMethodInfo
     * @param proxyClassInfo
     */
    private void addMethodBody(MethodNode mn, boolean isRunOnMainThread, MethodInfo newMethodInfo
            , ThreadScheduleProxyClass.ProxyClassInfo proxyClassInfo) {
        int returnOpcodec = ASMUtil.getOpcodecReturnValue(newMethodInfo.desc);

        // 如果设置了delay参数，直接走代理类
        if (mAnnotationInfo.delay <= 0) {
            LabelNode elseLabel = new LabelNode();
            // =============== if分支 ======================
            // 判断当前线程是否是主线程
            mn.instructions.add(getThreadUtilInstance());
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL
                    , Const.CLASS_NAME_THREAD_SCHEDULE_UTIL
                    , Const.METHOD_IS_MAIN_THREAD
                    , Const.DESC_METHOD_IS_MAIN_THREAD
                    , false));
            if (isRunOnMainThread) {
                mn.instructions.add(new JumpInsnNode(Opcodes.IFEQ, elseLabel));
            } else {
                mn.instructions.add(new JumpInsnNode(Opcodes.IFNE, elseLabel));
            }
            // 如果和isRunOnMainThread匹配，则直接调用newMethodInfo的方法
            if (!newMethodInfo.isStatic) {
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            }
            mn.instructions.add(ASMUtil.loadAllVar(newMethodInfo.access, newMethodInfo.desc));
            int opcode = newMethodInfo.isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
            mn.instructions.add(new MethodInsnNode(opcode
                    , mClassName
                    , newMethodInfo.name
                    , newMethodInfo.desc
                    , false));
            mn.instructions.add(new InsnNode(returnOpcodec));

            // =============== else分支 ======================
            // 如果和isRunOnMainThread不匹配，则创建代理类，并通过线程池工具来调用
            mn.instructions.add(elseLabel);
        }

        mn.instructions.add(runOnThreadScheduler(proxyClassInfo.instance
                , newMethodInfo
                , isRunOnMainThread ? Const.METHOD_RUN_ON_MAIN_THREAD : Const.METHOD_RUN_ON_BG_THREAD
                , isRunOnMainThread ? Const.DESC_METHOD_RUN_ON_MAIN_THREAD : Const.DESC_METHOD_RUN_ON_BG_THREAD
                , mAnnotationInfo.delay));
        mn.instructions.add(new InsnNode(returnOpcodec));
    }

    /**
     * 调用线程工具类进行线程调度
     *
     * @param proxyClass
     * @param methodInfo
     * @param method
     * @param methodDesc
     * @param delay
     * @return
     */
    private InsnList runOnThreadScheduler(ThreadScheduleProxyClass proxyClass, MethodInfo methodInfo
            , String method, String methodDesc, long delay) {
        InsnList list = new InsnList();

        // 判断是否有返回值，如果有返回值，创建阻塞器
        if (methodInfo.hasReturnValue) {
            // 实例化TryCatchCountDownLatch对象
            list.add(new TypeInsnNode(Opcodes.NEW, Const.CLASS_NAME_TRY_CATCH_COUNTDOWN_LATCH));
            list.add(new InsnNode(Opcodes.DUP));
            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL
                    , Const.CLASS_NAME_TRY_CATCH_COUNTDOWN_LATCH
                    , "<init>"
                    , "()V"
                    , false));
            mTryCountDownLatchInstanceStoreIndex = proxyClass.getProxyInstanceStoreIndex() + 1;
            list.add(new VarInsnNode(Opcodes.ASTORE, mTryCountDownLatchInstanceStoreIndex));
        }

        // 实例化代理类并调用他的init方法
        list.add(proxyClass.newProxyInstance());

        // 如果有返回值，调用setCountDownLatch方法
        if (methodInfo.hasReturnValue) {
            // 调用代理类的setCountDownLatch方法
            list.add(proxyClass.loadProxyInstance());
            list.add(new VarInsnNode(Opcodes.ALOAD, mTryCountDownLatchInstanceStoreIndex));
            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL
                    , proxyClass.getProxyClassName()
                    , Const.METHOD_SET_COUNT_DOWN_LATCH
                    , Const.DESC_METHOD_SET_COUNT_DOWN_LATCH
                    , false));
        }

        list.add(proxyClass.invokeInitMethod());


        // 加载线程池实例
        list.add(getThreadUtilInstance());
        // 传递delay参数
        list.add(new LdcInsnNode(delay));
        // 加载代理类实例
        list.add(proxyClass.loadProxyInstance());
        // 调用线程池对应的方法
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL
                , Const.CLASS_NAME_THREAD_SCHEDULE_UTIL
                , method
                , methodDesc
                , false));
        // 判断是否有返回值，如果有则阻塞等待异步调用结束
        if (methodInfo.hasReturnValue) {
            list.add(new VarInsnNode(Opcodes.ALOAD, mTryCountDownLatchInstanceStoreIndex));
            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL
                    , Const.CLASS_NAME_TRY_CATCH_COUNTDOWN_LATCH
                    , Const.METHOD_AWAIT
                    , Const.DESC_METHOD_AWAIT
                    , false));
            list.add(proxyClass.loadCountDownLatchField());
        }
        return list;
    }

    private InsnList getThreadUtilInstance() {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC
                , Const.CLASS_NAME_THREAD_SCHEDULE_UTIL
                , Const.METHOD_GET_INSTANCE
                , Const.DESC_METHOD_GET_INSTANCE
                , false));
        return list;
    }

    /**
     * 根据当前方法信息创建一个对应的代理类
     *
     * @param sourceClassName
     * @param newMethodInfo
     * @return
     */
    private ThreadScheduleProxyClass.ProxyClassInfo createMethodProxyClass(String sourceClassName, MethodInfo newMethodInfo) {
        return new ThreadScheduleProxyClass(sourceClassName, newMethodInfo).create();
    }

    /**
     * 创建一个相同方法体和签名的新方法
     *
     * @param cn
     * @param mn
     * @return
     */
    private MethodInfo createNewMethod(ClassNode cn, MethodNode mn) {
        String newMethodName = mn.name + "_" + Math.abs(mn.desc.hashCode()) + "_new";
        boolean isStatic = ASMUtil.isStaticMethod(mn.access);
        // 这里生成的方法是给外部的代理类调用的，需要是public的
        int access = isStatic ? (Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC) : Opcodes.ACC_PUBLIC;
        MethodNode newMethodNode = ASMUtil.cloneMethod(access, newMethodName, mn);
        cn.methods.add(newMethodNode);
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.name = newMethodName;
        methodInfo.desc = mn.desc;
        methodInfo.access = access;
        methodInfo.isStatic = isStatic;
        methodInfo.hasReturnValue = !mn.desc.endsWith("V");
        methodInfo.returnDesc = Type.getReturnType(mn.desc).getDescriptor();
        return methodInfo;
    }

    @Override
    public boolean visitInsnMode(ClassNode cn, AbstractInsnNode abstractInsnNode, MethodInfo methodInfo) {
        return false;
    }

    @Override
    public InsnList onMethodEnd(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
        return null;
    }
}
