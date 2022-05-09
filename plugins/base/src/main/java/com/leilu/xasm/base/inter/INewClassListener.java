package com.leilu.xasm.base.inter;

public interface INewClassListener {
    void onTransmitParams();

    Class<?>[] getParamTypes();
}
