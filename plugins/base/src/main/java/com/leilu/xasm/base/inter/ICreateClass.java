package com.leilu.xasm.base.inter;

/**
 * 创建类
 */
public interface ICreateClass extends IAddMethod, IAddField {

    /**
     * 获取到最终字节码数据
     *
     * @return
     */
    byte[] toByteArray();

    /**
     * 添加类上的注解
     *
     * @param listener
     */
    void addAnnotation(IAddAnnotation listener);

}
