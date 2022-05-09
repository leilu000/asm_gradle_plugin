package com.leilu.xasm.base.impl.modify.bean;


import com.leilu.xasm.ASMUtil;
import com.leilu.xasm.base.inter.IHook;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.Objects;

public class HookMethodData implements Cloneable {
    public String methodName;
    public Class<?> returnType;
    public Class<?>[] parameterTypes;
    public IHook.OnHookMethodListener listener;
    public String describer;
    public String interfaceName;
    public InvokeDynamicInsnNode node;

    public HookMethodData(String methodName, Class<?> returnType, Class<?>[] parameterTypes, IHook.OnHookMethodListener listener) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.listener = listener;
        this.describer = ASMUtil.getMethodDescriber(returnType, parameterTypes);
    }

    public HookMethodData(String methodName, String describer, String interfaceName, IHook.OnHookMethodListener listener) {
        this.methodName = methodName;
        this.describer = describer;
        this.interfaceName = interfaceName;
        this.listener = listener;
    }

    @Override
    public HookMethodData clone() {
        try {
            return (HookMethodData) super.clone();
        } catch (CloneNotSupportedException e) {
            return new HookMethodData(methodName, describer, interfaceName, listener);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HookMethodData that = (HookMethodData) o;
        return methodName.equals(that.methodName) &&
                describer.equals(that.describer) &&
                interfaceName.equals(that.interfaceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, describer, interfaceName);
    }
}
