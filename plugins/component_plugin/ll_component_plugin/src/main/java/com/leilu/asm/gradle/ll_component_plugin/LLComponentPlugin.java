package com.leilu.asm.gradle.ll_component_plugin;

import com.android.build.api.transform.Status;
import com.leilu.base.BaseExtensionInfo;
import com.leilu.base.BasePlugin;

import java.util.jar.JarOutputStream;

public class LLComponentPlugin extends BasePlugin<LLComponentExtension> {

    @Override
    protected byte[] modifyJarInputClass(String className, JarOutputStream jos, byte[] sourceData, Status status) {
        System.out.println("modifyJarInputClass:" + className + "  status:" + status);
        return sourceData;
    }

    @Override
    protected byte[] modifyDirectorInputClass(String className, String destDir, byte[] sourceData, Status status) {
        System.out.println("----------------modifyDirectorInputClass:" + className + "  status:" + status);
        return sourceData;
    }

    @Override
    protected LLComponentExtension initSelfDefineExtension() {
        return new LLComponentExtension();
    }

}
