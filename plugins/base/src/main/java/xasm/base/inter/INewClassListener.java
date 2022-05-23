package xasm.base.inter;

public interface INewClassListener {
    void onTransmitParams();

    Class<?>[] getParamTypes();
}
