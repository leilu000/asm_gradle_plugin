package xasm.base.impl.modify.bean;


import xasm.base.inter.IAddField;

import java.util.Objects;

public class AddFieldData {
    public int access;
    public String name;
    public String describer;
    public Object defaultValue;
    public IAddField.OnAddFiledListener listener;

    public AddFieldData(int access, String name, String describer) {
        this.access = access;
        this.name = name;
        this.describer = describer;
    }

    public AddFieldData(int access, String name, String describer, Object defaultValue) {
        this.access = access;
        this.name = name;
        this.describer = describer;
        this.defaultValue = defaultValue;
    }

    public AddFieldData(int access, String name, String describer, Object defaultValue, IAddField.OnAddFiledListener listener) {
        this.access = access;
        this.name = name;
        this.describer = describer;
        this.defaultValue = defaultValue;
        this.listener = listener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddFieldData data = (AddFieldData) o;
        return Objects.equals(name, data.name) &&
                Objects.equals(describer, data.describer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, describer);
    }
}
