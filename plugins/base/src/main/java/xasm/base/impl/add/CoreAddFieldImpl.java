package xasm.base.impl.add;

import xasm.ASMUtil;
import xasm.XASM;
import xasm.base.inter.IAddField;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;


import java.util.ArrayList;
import java.util.List;

/**
 * 使用core api的方式创建属性
 */
public class CoreAddFieldImpl implements IAddField {

    private final ClassWriter mClassWriter;
    // 防止重复添加，将已经添加的数据存放到这个集合
    private final List<String> mNameList = new ArrayList<>();

    public CoreAddFieldImpl(ClassWriter cw) {
        mClassWriter = cw;
    }

    @Override
    public void addField(int access, String name, String desc, Object defaultValue) {
        addField(access, name, desc, defaultValue, null);
    }

    @Override
    public void addFields(int access, String varPrefix, Type[] types) {
        if (mNameList.contains(varPrefix)) {
            XASM.getInstance().getLogger().w("Please dont add repeatedly the field varPrefix name:" + varPrefix);
            return;
        }
        mNameList.add(varPrefix);
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            FieldVisitor fv = mClassWriter.visitField(access, varPrefix + i, type.getDescriptor()
                    , null, null);
            fv.visitEnd();
        }
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
