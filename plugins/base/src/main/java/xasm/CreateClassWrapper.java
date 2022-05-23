package xasm;

import xasm.base.impl.SimpleOnCoreAddMethodListener;
import xasm.base.impl.add.CoreCreateClassImpl;
import xasm.base.inter.IAddAnnotation;
import xasm.base.inter.IAddField;
import xasm.base.inter.ICreateClass;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 创建类的包装类，主要是方便链式调用
 */
public class CreateClassWrapper {


    private final ICreateClass mAddASM;

    /**
     * 默认会创建无参构造方法，并没有方法体
     *
     * @param className 类名
     */
    public CreateClassWrapper(String className) {
        this(className, null, null, null);
    }

    /**
     * 默认会创建无参构造方法
     *
     * @param className 类名
     * @param listener  根据需要复写响应方法类创建方法体等
     */
    public CreateClassWrapper(String className, SimpleOnCoreAddMethodListener listener) {
        this(className, null, null, listener);
    }

    /**
     * 默认会创建无参构造方法
     *
     * @param className  类名
     * @param superClass 要继承的父类 如果为null，则默认是Object
     * @param interfaces 接口
     * @param listener   根据需要复写响应方法类创建方法体等
     */
    public CreateClassWrapper(String className, String superClass, String[] interfaces, SimpleOnCoreAddMethodListener listener) {
        this(Opcodes.V1_8, className, superClass, interfaces, listener);
    }

    /**
     * 默认会创建无参构造方法
     *
     * @param javaVersion java版本: Opcodes.V1_8等
     * @param className   类名
     * @param superClass  要继承的父类 如果为null，则默认是Object
     * @param interfaces  接口
     * @param listener    根据需要复写响应方法类创建方法体等
     */
    public CreateClassWrapper(int javaVersion, String className, String superClass, String[] interfaces, SimpleOnCoreAddMethodListener listener) {
        mAddASM = new CoreCreateClassImpl(javaVersion, className, superClass, interfaces, listener);
    }

    /**
     * 添加构造方法
     *
     * @param access   访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param desc     方法签名
     * @param listener 根据需要复写响应方法类创建方法体等
     * @return
     */
    public CreateClassWrapper addConstructorMethod(int access, String desc, SimpleOnCoreAddMethodListener listener) {
        addConstructorMethod(access, desc, null, listener);
        return this;
    }

    /**
     * 添加构造方法
     *
     * @param access     访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param desc       方法签名
     * @param exceptions 异常数组，没有设置为null
     * @param listener   根据需要复写响应方法类创建方法体等
     * @return
     */
    public CreateClassWrapper addConstructorMethod(int access, String desc, String[] exceptions
            , SimpleOnCoreAddMethodListener listener) {
        mAddASM.addConstructorMethod(access, desc, exceptions, listener);
        return this;
    }

    /**
     * 添加方法（不带抛出异常）
     *
     * @param access   访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param name     方法名
     * @param desc     方法签名
     * @param listener 根据需要复写响应方法类创建方法体等
     * @return
     */
    public CreateClassWrapper addMethod(int access, String name, String desc, SimpleOnCoreAddMethodListener listener) {
        addMethod(access, name, desc, null, listener);
        return this;
    }

    /**
     * 添加方法（带有抛出异常）
     *
     * @param access     访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param name       方法名
     * @param desc       方法签名
     * @param exceptions 异常数组，没有设置为null
     * @param listener   根据需要复写响应方法类创建方法体等
     * @return
     */
    public CreateClassWrapper addMethod(int access, String name, String desc
            , String[] exceptions, SimpleOnCoreAddMethodListener listener) {
        mAddASM.addMethod(access, name, desc, exceptions, listener);
        return this;
    }

    /**
     * 添加属性，并添加注解
     *
     * @param access       访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param name         属性名
     * @param desc         方法签名
     * @param defaultValue 默认值
     * @param listener     添加注解的回调,在里面返回需要添加的属性的数组
     * @return
     */
    public CreateClassWrapper addField(int access, String name, String desc, Object defaultValue
            , IAddField.OnAddFiledListener listener) {
        mAddASM.addField(access, name, desc, defaultValue, listener);
        return this;
    }

    /**
     * 添加属性
     *
     * @param access       访问标识：Opcodes.ACC_PUBLIC、Opcodes.ACC_PRIVATE等等
     * @param name         属性名
     * @param desc         方法签名
     * @param defaultValue 默认值
     * @return
     */
    public CreateClassWrapper addField(int access, String name, String desc, Object defaultValue) {
        mAddASM.addField(access, name, desc, defaultValue, null);
        return this;
    }

    /**
     * 添加类的注解
     *
     * @param listener
     * @return
     */
    public CreateClassWrapper addAnnotation(IAddAnnotation listener) {
        mAddASM.addAnnotation(listener);
        return this;
    }

    /**
     * 根据方法参数生成的type数组批量创建属性
     *
     * @param access    创建的属性的访问权限
     * @param varPrefix 属性的前缀，比如数组有3个长度，创建的属性根据下标依次
     *                  为：varPrefix0、varPrefix1、varPrefix2...
     * @param types
     * @return
     */
    public CreateClassWrapper addFields(int access, String varPrefix, Type[] types) {
        if (types == null || types.length == 0) {
            return this;
        }
        mAddASM.addFields(access, varPrefix, types);
        return this;
    }

    /**
     * 根据方法签名批量创建属性
     *
     * @param access
     * @param varPrefix 属性的前缀，比如数组有3个长度，创建的属性根据下标依次
     *                  为：varPrefix0、varPrefix1、varPrefix2...
     * @param desc
     * @return
     */
    public CreateClassWrapper addFields(int access, String varPrefix, String desc) {
        if (desc == null || "".equals(desc)) {
            return this;
        }
        return addFields(access, varPrefix, Type.getArgumentTypes(desc));
    }


    /**
     * 获取最终结果数据
     *
     * @return
     */
    public byte[] toByteArray() {
        return mAddASM.toByteArray();
    }
}
