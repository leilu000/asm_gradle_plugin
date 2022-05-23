package xasm.base.impl.modify.bean;


import org.objectweb.asm.Type;

import java.util.Objects;

public class PatchAddFieldData {
    public int access;
    public String varPrefix;
    public Type[] types;

    public PatchAddFieldData(int access, String varPrefix, Type[] types) {
        this.access = access;
        this.varPrefix = varPrefix;
        this.types = types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatchAddFieldData that = (PatchAddFieldData) o;
        return Objects.equals(varPrefix, that.varPrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varPrefix);
    }
}
