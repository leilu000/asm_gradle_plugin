package com.leilu.asm.gradle.ll_component_plugin;

import com.android.build.api.transform.Status;
import com.leilu.base.BaseExtensionInfo;
import com.leilu.base.BasePlugin;

import java.util.jar.JarOutputStream;

public class LLComponentPlugin extends BasePlugin {

    @Override
    protected byte[] modifyJarInputClass(String className, JarOutputStream jos, byte[] sourceData, Status status) {
        return new byte[0];
    }

    @Override
    protected byte[] modifyDirectorInputClass(String className, String destDir, byte[] sourceData, Status status) {
        return new byte[0];
    }

    @Override
    protected BaseExtensionInfo initSelfDefineExtension() {
        return null;
    }

}
