package com.leilu.xasm.base.impl.modify;

import android.util.Pair;

import com.leilu.xasm.ASMUtil;
import com.leilu.xasm.base.Const;
import com.leilu.xasm.base.impl.modify.bean.HookMethodData;
import com.leilu.xasm.base.impl.modify.bean.HookMethodWithAnnotation;
import com.leilu.xasm.base.impl.modify.bean.MethodInfo;
import com.leilu.xasm.base.inter.IHook;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.Consumer;

public class HookImpl implements IHook {
    // 保存需要hook的方法列表
    private final List<HookMethodData> mPaddingHookMethodList = new ArrayList<>();
    // 保存需要hook的注解列表
    private final List<HookMethodWithAnnotation> mPaddingHookAnnotationList = new ArrayList<>();

    @Override
    public void hookMethod(String methodName, Class<?> returnType, Class<?>[] parameterTypes, OnHookMethodListener listener) {
        if (methodName == null || listener == null) {
            throw new IllegalArgumentException("The methodName or listener is null !");
        }
        HookMethodData data = new HookMethodData(methodName, returnType, parameterTypes, listener);
        if (!mPaddingHookMethodList.contains(data)) {
            mPaddingHookMethodList.add(data);
        }
    }

    @Override
    public void hookMethod(String methodName, String describer, OnHookMethodListener listener) {
        hookMethod(methodName, describer, null, listener);
    }

    @Override
    public void hookMethod(String methodName, String describer, String interfaceName, OnHookMethodListener listener) {
        if (methodName == null || listener == null) {
            throw new IllegalArgumentException("The methodName or listener is null !");
        }
        HookMethodData data = new HookMethodData(methodName, describer, interfaceName, listener);
        if (!mPaddingHookMethodList.contains(data)) {
            mPaddingHookMethodList.add(data);
        }
    }

    @Override
    public void hookMethodWithAnnotations(List<String> annotationDescriber, OnHookMethodWithAnnotationListener listener) {
        if (annotationDescriber == null || annotationDescriber.size() == 0 || listener == null) {
            throw new IllegalArgumentException("The annotationDescriber or listener is null !");
        }
        mPaddingHookAnnotationList.add(new HookMethodWithAnnotation(annotationDescriber, listener));
    }

    private HookMethodData getHookMethodData(MethodNode mn) {
        // 遍历非lambda表达式
        for (HookMethodData data : mPaddingHookMethodList) {
            if (mn.name.equals(data.methodName) && mn.desc.equals(data.describer)) {
                return data;
            }
        }
        return null;
    }

    private HookMethodWithAnnotation getHookMethodWithAnnotationData(List<AnnotationNode> list) {
        if (list == null) {
            return null;
        }
        for (HookMethodWithAnnotation data : mPaddingHookAnnotationList) {
            for (AnnotationNode an : list) {
                if (data.desc.contains(an.desc)) {
                    data.annotationMap.put(an.desc, an.values);
                }
            }
            if (data.annotationMap.size() != 0) {
                return data;
            }
        }
        return null;
    }

    @Override
    public byte[] startHook(byte[] sourceData) {
        byte[] result = hook(sourceData, cn -> hookAnnotation(cn, cn.methods));
        return hook(result, cn -> hookMethod(cn, cn.methods));
    }

    private byte[] hook(byte[] sourceData, Consumer<ClassNode> consumer) {
        ClassReader cr = new ClassReader(sourceData);
        ClassNode cn = new ClassNode(Opcodes.ASM7);
        cr.accept(cn, ClassReader.SKIP_DEBUG);
        consumer.accept(cn);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cn.accept(cw);
        return cw.toByteArray();
    }

    private void hookAnnotation(ClassNode cn, List<MethodNode> methodNodes) {
        List<Pair<MethodNode, HookMethodWithAnnotation>> list = new ArrayList<>();
        for (MethodNode mn : methodNodes) {
            HookMethodWithAnnotation data = getHookMethodWithAnnotationData(mn.visibleAnnotations);
            if (data == null || data.annotationMap.size() == 0) {
                continue;
            }
            list.add(new Pair<>(mn, data));
        }
        for (Pair<MethodNode, HookMethodWithAnnotation> pair : list) {
            realHook(cn, pair.first, new OnHookMethodListener() {
                @Override
                public InsnList onMethodStart(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
                    return pair.second.listener.onMethodStart(cn, mn, methodInfo);
                }

                @Override
                public boolean visitInsnNode(ClassNode cn, AbstractInsnNode abstractInsnNode, MethodInfo methodInfo) {
                    return pair.second.listener.visitInsnMode(cn, abstractInsnNode, methodInfo);
                }

                @Override
                public InsnList onMethodEnd(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
                    return pair.second.listener.onMethodEnd(cn, mn, methodInfo);
                }
            });

        }
    }

    private void hookMethod(ClassNode cn, List<MethodNode> methodNodes) {
        // 先查找是否有匹配的非lambda表达式对应的方法，如果有则hook
        List<Pair<MethodNode, HookMethodData>> list = new ArrayList<>();
        for (MethodNode mn : methodNodes) {
            HookMethodData data = getHookMethodData(mn);
            if (data == null) {
                continue;
            }
            list.add(new Pair<>(mn, data));

        }
        for (Pair<MethodNode, HookMethodData> pair : list) {
            realHook(cn, pair.first, new HookMethodListener(cn, pair.second));
        }
        // 再查找是否有匹配的lambda表达式的方法，有则hook
        hookLambdaMethod(cn, methodNodes);
    }

    private void hookLambdaMethod(ClassNode cn, List<MethodNode> methodNodes) {
        List<HookMethodData> list = new ArrayList<>();
        // 查找是否有lambda表达式对应的方法
        for (MethodNode mn : methodNodes) {
            ListIterator<AbstractInsnNode> listIterator = mn.instructions.iterator();
            while (listIterator.hasNext()) {
                AbstractInsnNode an = listIterator.next();
                if (an instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode idi = (InvokeDynamicInsnNode) an;
                    // 如果方法和接口都匹配，则说明找到了通过lambda表达式实现的方法
                    for (HookMethodData data : mPaddingHookMethodList) {
                        if (idi.name.equals(data.methodName)
                                && data.interfaceName != null
                                && !"".equals(data.interfaceName)
                                && idi.desc.contains(data.interfaceName)) {
                            HookMethodData cloneData = data.clone();
                            cloneData.node = idi;
                            list.add(cloneData);
                        }
                    }
                }
            }
        }
        List<Pair<MethodNode, HookMethodData>> pairList = new ArrayList<>();
        // 遍历找到的lambda表达式实现的方法，然后拿到对应的方法句柄，看方法名和签名是否一样，是则进行hook
        for (MethodNode mn : methodNodes) {
            for (HookMethodData data : list) {
                Handle handle = (Handle) data.node.bsmArgs[1];
                if (mn.name.equals(handle.getName()) && mn.desc.equals(handle.getDesc())) {
                    pairList.add(new Pair<>(mn, data));
                }

            }
        }
        for (Pair<MethodNode, HookMethodData> pair : pairList) {
            realHook(cn, pair.first, new HookMethodListener(cn, pair.second));
        }
    }

    private MethodInfo createMethodInfo(MethodNode mn) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.isStatic = ASMUtil.isStaticMethod(mn.access);
        methodInfo.parameterTypes = Type.getArgumentTypes(mn.desc);
        List<AnnotationNode> annotationNodes = mn.visibleAnnotations;
        if (annotationNodes != null) {
            Map<String, Map<String, Object>> map = methodInfo.annotationMap;
            for (AnnotationNode an : annotationNodes) {
                Map<String, Object> kvMap = new HashMap<>();
                List<Object> values = an.values;
                if (values != null) {
                    for (int i = 0; i < values.size() - 1; i += 2) {
                        kvMap.put(values.get(i).toString(), values.get(i + 1));
                    }
                }
                map.put(ASMUtil.getASMClassName(an.desc), kvMap);
            }
        }
        return methodInfo;
    }

    private static class HookMethodListener implements OnHookMethodListener {

        private HookMethodData mData;
        private ClassNode mClassNode;

        public HookMethodListener(ClassNode classNode, HookMethodData data) {
            mClassNode = classNode;
            mData = data;
        }

        @Override
        public InsnList onMethodStart(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
            return mData.listener.onMethodStart(cn, mn, methodInfo);
        }

        @Override
        public boolean visitInsnNode(ClassNode cn, AbstractInsnNode abstractInsnNode, MethodInfo methodInfo) {
            return mData.listener.visitInsnNode(cn, abstractInsnNode, methodInfo);
        }

        @Override
        public InsnList onMethodEnd(ClassNode cn, MethodNode mn, MethodInfo methodInfo) {
            return mData.listener.onMethodEnd(cn, mn, methodInfo);
        }
    }

    private void realHook(ClassNode cn, MethodNode mn, IHook.OnHookMethodListener listener) {
        // 检查方法合法性
        if (ASMUtil.isAbsOrNativeMethod(mn.name, mn.access)) {
            return;
        }

        // 构建一个MethodInfo对象
        MethodInfo methodInfo = createMethodInfo(mn);

        InsnList instructions = mn.instructions;
        InsnList insertStartList = listener.onMethodStart(cn, mn, methodInfo);
        // 在方法开始之前插入
        if (insertStartList != null) {
            instructions.insert(insertStartList);
        }

        ListIterator<AbstractInsnNode> listIterator = instructions.iterator();
        while (listIterator.hasNext()) {
            AbstractInsnNode abstractInsnNode = listIterator.next();

            boolean needRemove = listener.visitInsnNode(cn, abstractInsnNode, methodInfo);

            // 遍历方法体指令，在回调里面进行替换等操作
            if (needRemove) {
                listIterator.remove();
            }

            // 在方法结束之后插入
            if (ASMUtil.isMethodEnd(abstractInsnNode)) {
                InsnList insertEndList = listener.onMethodEnd(cn, mn, methodInfo);

                // 如果删除了return语句，则判断是否需要自动帮用户添加return语句
                if (needRemove && !isContainsReturnInsn(insertEndList)) {
                    if (insertEndList == null) {
                        insertEndList = new InsnList();
                    }
                    Type returnType = Type.getReturnType(mn.desc);
                    if (returnType.getSort() == Type.VOID) {
                        insertEndList.add(new InsnNode(Opcodes.RETURN));
                    } else if (returnType.getSort() >= Type.BOOLEAN && returnType.getSort() <= Type.DOUBLE) {
                        insertEndList.add(new InsnNode(Opcodes.ICONST_0));
                        insertEndList.add(new InsnNode(Opcodes.IRETURN));
                    } else {
                        insertEndList.add(new InsnNode(Opcodes.ACONST_NULL));
                        insertEndList.add(new InsnNode(Opcodes.ARETURN));
                    }
                }

                if (insertEndList != null) {
                    instructions.insertBefore(abstractInsnNode, insertEndList);
                }
            }
        }
    }

    private boolean isContainsReturnInsn(InsnList list) {
        if (list == null) {
            return false;
        }
        ListIterator<AbstractInsnNode> iterator = list.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            if (ASMUtil.isMethodEnd(node)) {
                return true;
            }
        }
        return false;
    }
}


