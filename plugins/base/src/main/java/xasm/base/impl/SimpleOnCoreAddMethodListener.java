package xasm.base.impl;


import xasm.base.inter.IAddMethod;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;

/**
 * 便于调用者选择性实现里面的某个方法
 */
public class SimpleOnCoreAddMethodListener implements IAddMethod.OnCoreAddMethodListener {

    /**
     * 添加具体的方法内容，已经处理好方法的开始和结束，只需要添加对应的方法体内容即可
     * 无需再调用visitCode
     * 无需再调用visitInsn(Opcodec.XXRETURN)
     * 无需再调用visitMaxs方法
     *
     * @param mv
     */
    @Override
    public void onAddMethodBody(ClassWriter cw, MethodVisitor mv) {

    }

    /**
     * 获取注解和注解对应的键值对
     * Key：注解的class
     * Value：注解的键值对
     *
     * @return
     */
    @Override
    public Map<String, Map<String, Object>> getAnnotationsKeyValue() {
        return null;
    }

}
