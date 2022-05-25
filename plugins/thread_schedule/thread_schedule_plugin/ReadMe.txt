本module依赖了plugins->base这个module，所以如果要使用此插件，需要执行以下几步：

1、找到plugins->base->publishing下的publish任务，双击发布

2、找到plugins->thread_schedule->publishing下的publish任务，双击发布
通过以上两步，可以看到在plugins下会生成一个repos目录，里面就是通过上面两步发布的插件

3、配置项目根目录下的build.gradle文件：
    在buildscript代码块的dependencies代码块中配置如下：
        classpath "com.leilu.asm.plugins:base:${asm_plugin_version}"
        classpath "com.leilu.asm.plugins:thread_schedule_plugin:${asm_plugin_version}"
    其中asm_plugin_version为在gradle.properties中配置的版本号，目前项目写的是1.0.0

4、在app模块的build.gradle配置如下：
    apply plugin: 'thread_schedule.plugin'
    dependencies {
        implementation project(":plugins:thread_schedule:libthread_schedule")
    }

5、在app初始化的时候初始化一个线程调度器
    ThreadScheduleUtil.getInstance().setThreadPool(IThreadPool)
    其中setThreadPool参数为需要实现IThreadPool接口的类，至于为什么要设置这个类
    可以参考libthread_schedule下的ThreadPool的注释

通过以上几步就可以使用了，可以通过注解的方式来实现线程调度
@BGThread：在子线程执行
@MainThread：在主线程执行
同时以上两个注解都兼容带有返回值的方法


