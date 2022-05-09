package com.leilu.xasm.base.impl.add;

import com.leilu.xasm.ASMUtil;
import com.leilu.xasm.XASM;
import com.leilu.xasm.base.inter.IAddField;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;


import java.util.ArrayList;
import java.util.List;

public class AddFieldImpl implements IAddField {

    private final ClassWriter mClassWriter;
    // 防止重复添加，将已经添加的数据存放到这个集合
    private final List<String> mNameList = new ArrayList<>();

    public AddFieldImpl(ClassWriter cw) {
        mClassWriter = cw;
    }

    @Override
    public void addField(int access, String name, String desc, Object defaultValue) {
        addField(access, name, desc, defaultValue, null);
    }

    @Override
    public void addField(int access, String name, String desc, Object defaultValue, OnAddFiledListener listener) {
        if (mNameList.contains(name)) {
            XASM.getInstance().getLogger().w("Please dont add repeatedly the field name:" + name);
            return;
        }
        mNameList.add(name);
        FieldVisitor fv = mClassWriter.visitField(access, name, desc, null, defaultValue);
        if (listener != null) {
            ASMUtil.addAnnotations(fv, listener, null);
        }
        fv.visitEnd();
    }

}
