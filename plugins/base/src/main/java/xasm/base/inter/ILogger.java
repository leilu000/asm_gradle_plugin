package xasm.base.inter;

/**
 * 日志相关
 */
public interface ILogger {

    void i(String tag, String msg);

    void d(String tag, String msg);

    void e(String tag, String msg);

    void w(String tag, String msg);

    void i(String msg);

    void d(String msg);

    void e(String msg);

    void w(String msg);

    void setEnable(boolean enable);

}
