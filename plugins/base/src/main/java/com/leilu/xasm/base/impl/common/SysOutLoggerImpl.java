package com.leilu.xasm.base.impl.common;


import com.leilu.xasm.base.inter.ILogger;

public class SysOutLoggerImpl implements ILogger {

    private boolean mEnabel = true;

    @Override
    public void setEnable(boolean enable) {
        mEnabel = enable;
    }

    @Override
    public void i(String tag, String msg) {
        printMsg(tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        printMsg(tag, msg);
    }

    @Override
    public void e(String tag, String msg) {
        printMsg(tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        printMsg(tag, msg);
    }

    @Override
    public void i(String msg) {
        printMsg(msg);
    }

    @Override
    public void d(String msg) {
        printMsg(msg);
    }

    @Override
    public void e(String msg) {
        printMsg(msg);
    }

    @Override
    public void w(String msg) {
        printMsg(msg);
    }

    private void printMsg(String msg) {
        printMsg(null, msg);
    }

    private void printMsg(String tag, String msg) {
        if (mEnabel) {
            if (tag == null) {
                System.out.println(msg);
            } else {
                System.out.println(tag + ":\n" + msg);
            }
        }
    }
}
