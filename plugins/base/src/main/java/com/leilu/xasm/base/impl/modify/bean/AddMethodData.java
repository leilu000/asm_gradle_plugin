package com.leilu.xasm.base.impl.modify.bean;


import com.leilu.xasm.base.impl.SimpleOnAddMethodListener;

import java.util.Objects;

public class AddMethodData {
    public int access;
    public String name;
    public String desc;
    public Class<?>[] exceptions;
    public SimpleOnAddMethodListener listener;

    public AddMethodData(int access, String name, String desc) {
        this(access, name, desc, null, null);
    }


    public AddMethodData(int access, String name, String desc, SimpleOnAddMethodListener listener) {
        this(access, name, desc, null, listener);
    }

    public AddMethodData(int access, String name, String desc, Class<?>[] exceptions, SimpleOnAddMethodListener listener) {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.exceptions = exceptions;
        this.listener = listener;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddMethodData that = (AddMethodData) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, desc);
    }
}
