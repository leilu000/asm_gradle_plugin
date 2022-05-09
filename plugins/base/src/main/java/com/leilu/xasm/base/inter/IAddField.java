package com.leilu.xasm.base.inter;

/**
 * 添加属性
 */
public interface IAddField {

    /**
     * 添加属性的回调接口，目前只实现了添加注解
     */
    interface OnAddFiledListener extends IAddAnnotation {

    }

    /**
     * 添加属性
     *
     * @param access       访问权限:Opcodec.ACC_PUBLIC。。。
     * @param name         属性名
     * @param desc         属性签名
     * @param defaultValue 默认值，添加Object属性的时候无效
     */
    void addField(int access, String name, String desc, Object defaultValue);

    /**
     * @param access       访问权限:Opcodec.ACC_PUBLIC。。。
     * @param name         属性名
     * @param desc         属性签名
     * @param defaultValue 默认值，添加Object属性的时候无效
     * @param listener
     */
    void addField(int access, String name, String desc, Object defaultValue, OnAddFiledListener listener);

}
