package com.leilu.base;

public abstract class BaseExtensionInfo {

    /**
     * 是否开启该插件
     */
    public boolean enable = true;

    /**
     * 是否显示日志
     */
    public boolean showLog = true;

    public abstract String getName();

}
