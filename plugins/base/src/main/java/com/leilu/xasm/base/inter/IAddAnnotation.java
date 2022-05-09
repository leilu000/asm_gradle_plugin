package com.leilu.xasm.base.inter;

import java.util.Map;

/**
 * 添加注解
 */
public interface IAddAnnotation {


    /**
     * 获取注解和注解对应的键值对
     * Key：注解的签名
     * Value：注解的键值对
     *
     * @return
     */
    Map<String, Map<String, Object>> getAnnotationsKeyValue();


    /**
     * 开始真正添加注解的回调
     */
    interface OnStartAddAnnotationListener {

        /**
         * 开始真正添加某个注解
         *
         * @param annotationDescriber
         */
        void onStartAddAnnotation(String annotationDescriber);
    }
}
