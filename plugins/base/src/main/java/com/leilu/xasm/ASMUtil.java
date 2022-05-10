package com.leilu.xasm;

import com.leilu.xasm.base.inter.IAddAnnotation;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ASMUtil {

    /**
     * 判断是否到达方法结尾
     *
     * @param node
     * @return
     */
    public static boolean isMethodEnd(AbstractInsnNode node) {
        return node.getOpcode() == Opcodes.ATHROW
                || node.getOpcode() >= Opcodes.IRETURN && node.getOpcode() <= Opcodes.RETURN;
    }

    /**
     * 如果是本地方法或者抽象方法，则抛出异常
     *
     * @param name
     * @param access
     * @return
     */
    public static boolean isAbsOrNativeMethod(String name, int access) {
        boolean isAbstractMethod = (access & Opcodes.ACC_ABSTRACT) != 0;
        boolean isNativeMethod = (access & Opcodes.ACC_NATIVE) != 0;
        return isAbstractMethod || isNativeMethod;
    }

    /**
     * tree api的方式传递参数
     *
     * @param obj
     * @return
     */
    public static AbstractInsnNode transmitParam(Object obj) {
        if (obj == null) {
            return new InsnNode(Opcodes.ACONST_NULL);
        }
        AbstractInsnNode node = null;
        if (obj instanceof Integer) {
            node = transmitNumber((int) obj);
        } else if (obj instanceof Byte) {
            node = transmitNumber((byte) obj);
        } else if (obj instanceof Short) {
            node = transmitNumber((short) obj);
        } else if (obj instanceof Boolean) {
            boolean b = (boolean) obj;
            node = transmitNumber(b ? 1 : 0);
        } else if (obj instanceof Float) {
            float i = (float) obj;
            if (i == 0) {
                node = new InsnNode(Opcodes.FCONST_0);
            } else if (i == 1) {
                node = new InsnNode(Opcodes.FCONST_1);
            } else if (i == 2) {
                node = new InsnNode(Opcodes.FCONST_2);
            } else {
                node = new LdcInsnNode(i);
            }
        } else if (obj instanceof Long) {
            long i = (long) obj;
            if (i == 0) {
                node = new InsnNode(Opcodes.LCONST_0);
            } else if (i == 1) {
                node = new InsnNode(Opcodes.LCONST_1);
            } else {
                node = new LdcInsnNode(i);
            }
        } else if (obj instanceof Double) {
            double i = (double) obj;
            if (i == 0) {
                node = new InsnNode(Opcodes.DCONST_0);
            } else if (i == 1) {
                node = new InsnNode(Opcodes.DCONST_1);
            } else {
                node = new LdcInsnNode(i);
            }
        } else {
            node = new LdcInsnNode(obj);
        }
        return node;
    }


    /**
     * core api的方式传递参数
     *
     * @param mv
     * @param obj
     */
    public static void transmitParam(MethodVisitor mv, Object obj) {
        if (obj == null) {
            mv.visitLdcInsn(null);
            return;
        }
        if (obj instanceof Integer) {
            transmitNumber(mv, (int) obj);
        } else if (obj instanceof Byte) {
            transmitNumber(mv, (byte) obj);
        } else if (obj instanceof Short) {
            transmitNumber(mv, (short) obj);
        } else if (obj instanceof Boolean) {
            boolean b = (boolean) obj;
            transmitNumber(mv, b ? 1 : 0);
        } else if (obj instanceof Float) {
            float i = (float) obj;
            if (i == 0) {
                mv.visitInsn(Opcodes.FCONST_0);
            } else if (i == 1) {
                mv.visitInsn(Opcodes.FCONST_1);
            } else if (i == 2) {
                mv.visitInsn(Opcodes.FCONST_2);
            } else {
                mv.visitLdcInsn(obj);
            }
        } else if (obj instanceof Long) {
            long i = (long) obj;
            if (i == 0) {
                mv.visitInsn(Opcodes.LCONST_0);
            } else if (i == 1) {
                mv.visitInsn(Opcodes.LCONST_1);
            } else {
                mv.visitLdcInsn(obj);
            }
        } else if (obj instanceof Double) {
            double i = (double) obj;
            if (i == 0) {
                mv.visitInsn(Opcodes.LCONST_0);
            } else if (i == 1) {
                mv.visitInsn(Opcodes.LCONST_1);
            } else {
                mv.visitLdcInsn(obj);
            }
        } else {
            mv.visitLdcInsn(obj);
        }
    }


    /**
     * 根据class类型得到Opcodes.XLOAD
     *
     * @param clazz
     * @return
     */
    public static int getLoadOpcode(Class<?> clazz) {
        if (clazz == boolean.class || clazz == int.class
                || clazz == byte.class || clazz == short.class || clazz == char.class) {
            return Opcodes.ILOAD;
        }
        if (clazz == float.class) {
            return Opcodes.FLOAD;
        }
        if (clazz == double.class) {
            return Opcodes.DLOAD;
        }
        if (clazz == long.class) {
            return Opcodes.LLOAD;
        }
        return Opcodes.ALOAD;
    }


    /**
     * 通过反射获取父类方法
     *
     * @param clazz          需要获取的类
     * @param methodName     方法名
     * @param returnType     返回类型,void.class或者xxx.class
     * @param parameterTypes 参数类型:xxx.class
     * @return
     */
    public static MethodHandle getSuperClassMethod(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Field allowedModes;
        try {
            // 这里需要把allowedModes通过反射设置为1，否则会包错
            allowedModes = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
            allowedModes.setAccessible(true);
            allowedModes.set(lookup, -1);
            return lookup.findSpecial(clazz, methodName, MethodType.methodType(returnType, parameterTypes), clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 检查是否需要删除对应的注解
    public static boolean checkNeedRemoveAnnotation(String descriptor, List<String> annotationClass) {
        // 如果注解类数组为空，则删除全部注解
        if (annotationClass == null || annotationClass.size() == 0) {
            return true;
        }
        for (String annotation : annotationClass) {
            if (descriptor.equals(annotation)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 根据类名获取Class
     *
     * @param className
     * @return
     */
    public static Class<?> getClassForName(String className) {
        try {
            return Class.forName(className.replaceAll("/", "\\."));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据描述符获取对应的class类型
     *
     * @param describer
     * @return
     */
    public static Class<?> getClassForDescriber(String describer) {
        if ("Z".equals(describer)) {
            return boolean.class;
        }
        if ("I".equals(describer)) {
            return int.class;
        }
        if ("S".equals(describer)) {
            return short.class;
        }
        if ("B".equals(describer)) {
            return byte.class;
        }
        if ("C".equals(describer)) {
            return char.class;
        }
        if ("D".equals(describer)) {
            return double.class;
        }
        if ("F".equals(describer)) {
            return float.class;
        }
        if ("L".equals(describer)) {
            return long.class;
        }
        try {
            describer = describer.substring(describer.indexOf("L") + 1, describer.lastIndexOf(";"));
            return Class.forName(describer);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取com/xxx/xxx格式的全路径包名
     *
     * @param clazz
     * @return com/xxx/xxx格式的全路径包名
     */
    public static String getASMClassFullName(Class<?> clazz) {
        return getASMClassFullName(clazz.getName());
    }

    /**
     * 将全路径包名转换成com/xxx/xxx格式的全路径包名
     *
     * @param fullClassName
     * @return com/xxx/xxx格式的全路径包名
     */
    public static String getASMClassFullName(String fullClassName) {
        return fullClassName.replaceAll("\\.", "/");
    }

    /**
     * 根据签名获取类名 xxx.xxx.xxx
     *
     * @param desc 类似：Ljava/lang/String;
     * @return
     */
    public static String getASMClassName(String desc) {
        if (!desc.startsWith("L") && !desc.endsWith(";")) {
            throw new IllegalArgumentException("The desc is invalidate !");
        }
        return desc.replace("L", "")
                .replace("/", ".")
                .replace(";", "");
    }

    /**
     * 为ClassVisitor 、MethodVisitor、FieldVisitor添加注解的工具类
     *
     * @param visitor                      只能是ClassVisitor 、MethodVisitor、FieldVisitor
     * @param addAnnotationListener
     * @param onStartAddAnnotationListener
     */
    public static boolean addAnnotations(Object visitor, IAddAnnotation addAnnotationListener
            , IAddAnnotation.OnStartAddAnnotationListener onStartAddAnnotationListener) {
        if (!(visitor instanceof MethodVisitor || visitor instanceof FieldVisitor || visitor instanceof ClassVisitor)) {
            throw new IllegalArgumentException("The visitor is wrong !");
        }
        // 获取到需要添加的注解class
        // 获取到注解的键值对集合
        Map<String, Map<String, Object>> annoKeyValues = addAnnotationListener.getAnnotationsKeyValue();
        if (annoKeyValues == null || annoKeyValues.size() == 0) {
            return false;
        }
        for (Map.Entry<String, Map<String, Object>> annoEntry : annoKeyValues.entrySet()) {
            String annoDesc = annoEntry.getKey();
            // 这里判断一下，如果填写的类不是注解类，则抛出异常
//            if (!annoDesc.isAnnotation()) {
//                throw new IllegalArgumentException("The " + annoDesc.getName() + " is not annotation !");
//            }
            // 将com.xx.xxx格式的类名转为 com/xx/xxx
//            String describer = getASMClassFullName(annoDesc);

            AnnotationVisitor av;
            if (onStartAddAnnotationListener != null) {
                onStartAddAnnotationListener.onStartAddAnnotation(annoDesc);
            }
            if (visitor instanceof FieldVisitor) {
                av = ((FieldVisitor) visitor).visitAnnotation(annoDesc, true);
            } else if (visitor instanceof ClassWriter) {
                av = ((ClassVisitor) visitor).visitAnnotation(annoDesc, true);
            } else {
                av = ((MethodVisitor) visitor).visitAnnotation(annoDesc, true);
            }

            if (av != null) {
                Map<String, Object> map = annoKeyValues.get(annoDesc);
                if (map != null) {
                    // 遍历，根据注解类的class获取到对应的键值对
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        // 检查一下键值对是否真实在注解类中存在，如果不存在，则抛出异常
//                        checkKeyAndValue(annoDesc, entry.getKey());
                        av.visit(entry.getKey(), entry.getValue());
                    }
                }
                av.visitEnd();
            }
        }
        return true;
    }

    /**
     * 关闭io
     *
     * @param io
     */
    public static void closeIO(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存字节码数据
     *
     * @param data
     * @param classPath
     */
    public static void saveClassData(byte[] data, String classPath) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(classPath);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(fos);
        }
    }

    /**
     * 通过类路径读取byte数据
     *
     * @param classPath
     * @return
     */
    public static byte[] getClassData(String classPath) {
        FileInputStream fos = null;
        ByteArrayOutputStream bos = null;
        try {
            fos = new FileInputStream(classPath);
            bos = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fos.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            fos.close();
            bos.close();
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(fos);
            closeIO(bos);
        }
        return null;
    }

    /**
     * 根据返回值类型获取ASM的Opcodes.XXX_RETURN
     *
     * @param desc
     * @return
     */
    public static int getOpcodecReturnValue(String desc) {
        String returnType = desc.substring(desc.lastIndexOf(")") + 1);
        if (returnType.equals("B")
                || returnType.equals("I")
                || returnType.equals("S")
                || returnType.equals("Z")
                || returnType.equals("C")) {
            return Opcodes.IRETURN;
        }
        if (returnType.equals("F")) {
            return Opcodes.FRETURN;
        }
        if (returnType.equals("J")) {
            return Opcodes.LRETURN;
        }
        if (returnType.equals("D")) {
            return Opcodes.DRETURN;
        }
        if (returnType.equals("V")) {
            return Opcodes.RETURN;
        }
        return Opcodes.ARETURN;
    }

    /**
     * 根据返回值类型获取ASM的Opcodes.XXX_RETURN
     *
     * @param returnType
     * @return
     */
    public static int getOpcodecReturnValue(Class<?> returnType) {
        if (returnType == byte.class || returnType == int.class
                || returnType == short.class || returnType == boolean.class || returnType == char.class) {
            return Opcodes.IRETURN;
        }
        if (returnType == float.class) {
            return Opcodes.FRETURN;
        }
        if (returnType == long.class) {
            return Opcodes.LRETURN;
        }
        if (returnType == double.class) {
            return Opcodes.DRETURN;
        }
        return Opcodes.ARETURN;
    }


    /**
     * 检查参数类型和传入的局部变量表是否匹配
     *
     * @param paramTypes
     * @param paramSlotIndexes
     */
    public static void checkParamType(Class<?>[] paramTypes, int[] paramSlotIndexes) {
        // 检查参数类型是否和局部变量表是一一对应的
        if (paramTypes != null && paramSlotIndexes != null
                && paramTypes.length != paramSlotIndexes.length) {
            throw new IllegalArgumentException("The paramTypes and paramSlotIndexes not match !");
        }
    }


    /**
     * 根据返回值类型和方法参数方法的签名
     *
     * @param returnType     返回值类型，如果为空，则没有返回值
     * @param parameterTypes 方法的参数类型（注意：是需要有顺序的）
     * @return
     */
    public static String getMethodDescriber(Class<?> returnType, Class<?>[] parameterTypes) {
        // 计算方法签名
        StringBuilder descBuilder = new StringBuilder()
                .append("(");
        if (parameterTypes != null && parameterTypes.length != 0) {
            for (Class<?> cls : parameterTypes) {
                descBuilder.append(ASMUtil.getDescriberByClass(cls));
            }
        }
        descBuilder.append(")")
                .append(returnType == null ? "V" : ASMUtil.getDescriberByClass(returnType));
        return descBuilder.toString();
    }


    /**
     * 根据class获取签名
     *
     * @param cls
     * @return
     */
    public static String getDescriberByClass(Class<?> cls) {
        if (cls == Byte.class || cls == byte.class) {
            return "B";
        }
        if (cls == Short.class || cls == short.class) {
            return "S";
        }
        if (cls == Character.class || cls == char.class) {
            return "C";
        }
        if (cls == Integer.class || cls == int.class) {
            return "I";
        }
        if (cls == Float.class || cls == float.class) {
            return "F";
        }
        if (cls == Double.class || cls == double.class) {
            return "D";
        }
        if (cls == Long.class || cls == long.class) {
            return "J";
        }
        if (cls == Boolean.class || cls == boolean.class) {
            return "Z";
        }
        return getObjectClassDescriber(cls);
    }

    /**
     * 根据对象获取签名
     *
     * @param obj
     * @return
     */
    public static String getDescriberByObject(Object obj) {
        if (obj instanceof Byte) {
            return "B";
        }
        if (obj instanceof Short) {
            return "S";
        }
        if (obj instanceof Character) {
            return "C";
        }
        if (obj instanceof Integer) {
            return "I";
        }
        if (obj instanceof Float) {
            return "F";
        }
        if (obj instanceof Double) {
            return "D";
        }
        if (obj instanceof Long) {
            return "J";
        }
        if (obj instanceof Boolean) {
            return "Z";
        }
        return getObjectClassDescriber(obj.getClass());
    }

    private static String getObjectClassDescriber(Class<?> cls) {
        return "L" + getASMClassFullName(cls) + ";";
    }


    private static void checkKeyAndValue(Class<?> clazz, String key) {
        Method[] methods = clazz.getDeclaredMethods();
        boolean has = false;
        for (Method method : methods) {
            if (method.getName().equals(key)) {
                has = true;
                break;
            }
        }
        if (!has) {
            throw new IllegalArgumentException("The key:" + key + " is not exist in " + clazz.getName());
        }
    }

    private static void transmitNumber(MethodVisitor mv, int i) {
        if (i == -1) {
            mv.visitInsn(Opcodes.ICONST_M1);
        } else if (i == 0) {
            mv.visitInsn(Opcodes.ICONST_0);
        } else if (i == 1) {
            mv.visitInsn(Opcodes.ICONST_1);
        } else if (i == 2) {
            mv.visitInsn(Opcodes.ICONST_2);
        } else if (i == 3) {
            mv.visitInsn(Opcodes.ICONST_3);
        } else if (i == 4) {
            mv.visitInsn(Opcodes.ICONST_4);
        } else if (i == 5) {
            mv.visitInsn(Opcodes.ICONST_5);
        } else if (i >= -128 && i <= 127) {
            mv.visitIntInsn(Opcodes.BIPUSH, i);
        } else if (i >= -32768 && i <= 32767) {
            mv.visitIntInsn(Opcodes.SIPUSH, i);
        } else {
            mv.visitLdcInsn(i);
        }
    }

    private static AbstractInsnNode transmitNumber(int i) {
        AbstractInsnNode node = null;
        if (i == -1) {
            node = new InsnNode(Opcodes.ICONST_M1);
        } else if (i == 0) {
            node = new InsnNode(Opcodes.ICONST_0);
        } else if (i == 1) {
            node = new InsnNode(Opcodes.ICONST_1);
        } else if (i == 2) {
            node = new InsnNode(Opcodes.ICONST_2);
        } else if (i == 3) {
            node = new InsnNode(Opcodes.ICONST_3);
        } else if (i == 4) {
            node = new InsnNode(Opcodes.ICONST_4);
        } else if (i == 5) {
            node = new InsnNode(Opcodes.ICONST_5);
        } else if (i >= -128 && i <= 127) {
            node = new IntInsnNode(Opcodes.BIPUSH, i);
        } else if (i >= -32768 && i <= 32767) {
            node = new IntInsnNode(Opcodes.SIPUSH, i);
        } else {
            node = new LdcInsnNode(i);
        }
        return node;
    }

    /**
     * 判断某个方法是否静态方法
     *
     * @param access
     * @return
     */
    public static boolean isStaticMethod(int access) {
        return (Opcodes.ACC_STATIC & access) != 0;
    }
}
