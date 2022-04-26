package com.leilu.asm.method_hook;

import com.android.build.api.transform.Status;
import com.leilu.base.BasePlugin;

public class MethodHookPlugin extends BasePlugin<MethodHookConfig> {

    @Override
    protected byte[] modifyClass(byte[] classData, Status status) {
        return classData;
    }

    @Override
    protected MethodHookConfig initSelfDefineExtension() {
        return new MethodHookConfig();
    }

}
