package com.leilu.xasm.base.impl.modify.bean;


import java.util.Objects;

public class ModifyData {
    public String name;
    public String desc;


    public ModifyData(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModifyData that = (ModifyData) o;
        if (that.desc == null || that.desc.equals("")) {
            return that.name.equals(name);
        }
        return Objects.equals(name, that.name) && Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, desc);
    }
}
