package com.leilu.xasm;


import com.leilu.xasm.base.impl.ASMHelperImpl;
import com.leilu.xasm.base.impl.SimpleOnAddMethodListener;
import com.leilu.xasm.base.impl.common.SysOutLoggerImpl;
import com.leilu.xasm.base.inter.IASMHelper;
import com.leilu.xasm.base.inter.ILogger;

/**
 * 通过此类可以实现ASM代码的增加和删除修改等
 */
public class XASM {

    private static final XASM INSTANCE = new XASM();

    private final ASMPrinter mASMPrinter;
    private final ILogger mLogger;
    private final IASMHelper mASMHelper;

    private XASM() {
        mASMPrinter = new ASMPrinter();
        mLogger = new SysOutLoggerImpl();
        mASMHelper = new ASMHelperImpl();
    }

    public static XASM getInstance() {
        return INSTANCE;
    }

    /**
     * 打印日志
     *
     * @return
     */
    public ILogger getLogger() {
        return mLogger;
    }

    /**
     * 获取新建对象、调用方法的封装的帮助类
     *
     * @return
     */
    public IASMHelper getASMHelper() {
        return mASMHelper;
    }

    /**
     * 获取添加System.out.println的工具类
     *
     * @return
     */
    public ASMPrinter getASMPrinter() {
        return mASMPrinter;
    }

    /**
     * 创建类，默认创建一个空构造方法
     *
     * @param className 类名
     * @return
     */
    public CreateClassWrapper createClass(String className) {
        return new CreateClassWrapper(className);
    }

    /**
     * 创建类，默认创建一个无参构造
     *
     * @param className 类名
     * @param listener  写自己的构造方法体
     * @return
     */
    public CreateClassWrapper createClass(String className, SimpleOnAddMethodListener listener) {
        return new CreateClassWrapper(className, listener);
    }

    /**
     * 创建类，默认创建一个无参构造
     *
     * @param className  类名
     * @param superClass 要继承的父类 如果为null，则默认是Object
     * @param interfaces 接口
     * @param listener   写自己的构造方法体
     * @return
     */
    public CreateClassWrapper createClass(String className, String superClass, String[] interfaces, SimpleOnAddMethodListener listener) {
        return new CreateClassWrapper(className, superClass, interfaces, listener);
    }

    /**
     * 创建类，默认创建一个无参构造
     *
     * @param javaVersion java版本：Opcodes.V1_8等
     * @param className   类名
     * @param superClass  要继承的父类 如果为null，则默认是Object
     * @param interfaces  接口
     * @param listener    写自己的构造方法体
     * @return
     */
    public CreateClassWrapper createClass(int javaVersion, String className, String superClass, String[] interfaces, SimpleOnAddMethodListener listener) {
        return new CreateClassWrapper(javaVersion, className, superClass, interfaces, listener);
    }


    /**
     * 修改类
     *
     * @param classPath 类的真实路径:xxx/xxx/xxx.class
     * @return
     */
    public ModifyClassWrapper modifyClass(String classPath) {
        return new ModifyClassWrapper(classPath);
    }

    /**
     * 修改类
     *
     * @param classData 类的字节码数组
     * @return
     */
    public ModifyClassWrapper modifyClass(byte[] classData) {
        return new ModifyClassWrapper(classData);
    }
}
