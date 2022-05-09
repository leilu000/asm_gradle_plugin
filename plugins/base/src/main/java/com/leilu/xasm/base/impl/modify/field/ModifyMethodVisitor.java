package com.leilu.xasm.base.impl.modify.field;

import com.leilu.xasm.base.Const;
import com.leilu.xasm.base.impl.modify.ModifyAnnotationHelper;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 修改方法的visitor
 */
public class ModifyMethodVisitor extends MethodVisitor {

    private final ModifyAnnotationHelper mModifyAnnotationHelper;


    public ModifyMethodVisitor(String methodMethod, String describer, MethodVisitor fv, ModifyAnnotationHelper helper) {
        super(Const.ASM_VERSION, fv);
        mModifyAnnotationHelper = helper;
        mModifyAnnotationHelper.setVisitor(this, methodMethod + describer);
    }


    // 如果该属性存在注解，走到这个方法；如果存在注解，则不会走到这个方法
    // 但如果在某处调用的该属性的visitAnnotation，则也会调用到此方法
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (!mModifyAnnotationHelper.visitAnnotation(descriptor, visible)) {
            return null;
        }
        return super.visitAnnotation(descriptor, true);
    }

    @Override
    public void visitEnd() {
        mModifyAnnotationHelper.visitEnd();
        super.visitEnd();
    }
}
