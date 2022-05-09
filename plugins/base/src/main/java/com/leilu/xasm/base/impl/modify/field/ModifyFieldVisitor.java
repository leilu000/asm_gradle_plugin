package com.leilu.xasm.base.impl.modify.field;

import com.leilu.xasm.base.Const;
import com.leilu.xasm.base.impl.modify.ModifyAnnotationHelper;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 修改属性的visitor
 */
public class ModifyFieldVisitor extends FieldVisitor {

    private final ModifyAnnotationHelper mModifyAnnotationHelper;


    public ModifyFieldVisitor(String fieldName, FieldVisitor fv, ModifyAnnotationHelper helper) {
        super(Const.ASM_VERSION, fv);
        mModifyAnnotationHelper = helper;
        mModifyAnnotationHelper.setVisitor(this, fieldName);
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
