package com.leilu.xasm.base.inter;

import java.util.Map;

/**
 * 修改类
 */
public interface IModifyClass extends IAddField, IAddMethod, IHook {

    /**
     * 获取修改好后的字节码数据
     *
     * @return
     */
    byte[] toByteArray();

    /**
     * 删除属性
     *
     * @param name
     */
    void removeField(String name);

    /**
     * 批量添加属性注解
     *
     * @param name          属性名
     * @param annotationMap 注解集合Map<String, Map<String, Object>>中第一个外层Map中Key代表注解的class
     *                      内层Map是Key对应的注解的键值对
     *                      比如：["Ljava/lang/Override"]
     */
    void addFieldAnnotation(String name, Map<String, Map<String, Object>> annotationMap);

    /**
     * 批量删除属性上的注解
     *
     * @param name              属性名
     * @param annotationClasses 需要删除的注解集合，如果集合为空，则全部删除
     */
    void removeFieldAnnotation(String name, String... annotationClasses);

    /**
     * 删除某个方法
     *
     * @param name 方法名
     * @param desc 方法签名
     */
    void removeMethod(String name, String desc);

    /**
     * 批量删除类上的注解
     *
     * @param annotationClasses 需要删除的注解集合，如果集合为空，则全部删除
     */
    void removeClassAnnotation(String... annotationClasses);

    /**
     * 批量添加类上注解
     *
     * @param annotations 注解集合Map<String, Map<String, Object>>中第一个外层Map中Key代表注解的class
     *                    内层Map是Key对应的注解的键值对
     *                    比如：["Ljava/lang/Override"]
     */
    void addClassAnnotation(Map<String, Map<String, Object>> annotations);

    /**
     * 批量添加方法上注解
     *
     * @param methodName    方法名
     * @param desc          方法签名
     * @param annotationMap 注解集合Map<String, Map<String, Object>>中第一个外层Map中Key代表注解的class
     *                      内层Map是Key对应的注解的键值对
     *                      比如：["Ljava/lang/Override"]
     */
    void addMethodAnnotation(String methodName, String desc, Map<String, Map<String, Object>> annotationMap);

    /**
     * 批量删除方法注解
     *
     * @param name              方法名
     * @param desc              方法签名
     * @param annotationClasses 需要删除的注解集合，如果集合为空，则全部删除
     */
    void removeMethodAnnotation(String name, String desc, String... annotationClasses);


}
