    一个基于ASM框架封装的插件项目
    主要项目结构如下：
        moudles ---此目录存放app和库工程，主要用来做测试，里面不含实际的功能代码
                ---app模块
                ---login模块
                ---testjava模块
                ---xxx模块

    plugins ---此目录是插件工程，所有的插件都写在此目录下，同时发布到本地的插件也是在此目录的repos目录下
            ---base 模块，此模块为插件的基础模块，主要分为两部分：
                1）、BasePlugin相关功能
                    支持增量编译、并发编译，如果要新增插件，则直接继承此类可以很方便的开发自己想要的插件
                2）、xasm框架
                    为了方便使用ASM插装工具，这里封装了一个基于ASM库的更便于使用的框架，使用链式调用，大大减少
                    模板代码的编写，该框架封装了core api和tree api两种调用方式，可以根据自己的喜好选择其中一种
                    进行字节码的插装工作，主要功能如下：
                    1、添加一个新的类:
                        添加：
                            构造方法、方法、属性、注解（类上的注解、属性上的注解、方法上的注解）
                        删除：
                            构造方法、方法、属性、注解（批量或者单个类上的注解、属性上的注解、方法上的注解）

                    2、修改一个存在的类：
                        添加：
                        构造方法、方法、属性、注解（类上的注解、属性上的注解、方法上的注解）
                        删除：
                        构造方法、方法、属性、注解（批量或者单个类上的注解、属性上的注解、方法上的注解）
                        hook：
                        修改属性
                        修改方法：
                        在方法开始插入代码、在方法中间插入代码、替换方法体中的类的创建和方法的调用、替换方法提
                        的全部代码（只需要新增一个相同名字和签名的方法，框架会删除旧的方法，增加新的方法）
            
            ---thread_schedule  模块，此模块是基于base模块编写的线程调度插件，通过gradle配置
                                （目前暂未实现）或者注解两种方式实现主、子线程切换，并支持带有返回值的方法
                                该模块包含两个子模块，如下：
                                1）、libthread_schedule
                                    此模块包含注解和线程工具类，提供给其他需要的模块引入，同时插件也会使用到这里的类
                                2）、thread_schedule_plugin
                                    这是真正的线程调度插件库，里面编写了线程调度逻辑，具体的调度思路如下：
                                        思路等我有空再写
                                
                                本插件使用方式：
                                    本module依赖了plugins->base这个module，所以如果要使用此插件，需要执行以下几步：
                                    1、找到plugins->base->publishing下的publish任务，双击发布
                                    
                                    2、找到plugins->thread_schedule->publishing下的publish任务，双击发布
                                    通过以上两步，可以看到在plugins下会生成一个repos目录，里面就是通过上面两步发布的插件
                                    
                                    3、配置项目根目录下的build.gradle文件：
                                    repositories {
                                        maven {
                                            url uri("${rootDir}/plugins/repos")
                                        }
                                    }
                                    dependencies {
                                        classpath "com.leilu.asm.plugins:base:${asm_plugin_version}"
                                        classpath "com.leilu.asm.plugins:thread_schedule_plugin:${asm_plugin_version}"
                                    }
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
                                    
            ---component_plugin 基于ASM的组件化框架，一种比ARouter更加轻量的方案
                libcompoment模块：
                                 1、任务启动框架：一个实现了可以通过依赖关系，并且实现不同线程之间并发运行的任务启动框架，以解决一个应用在启动的时候各个业务的耦合关系，并优化启动速度
                                             使用案例：
                                                new LaunchTaskManager()
                                                        // 添加具体的启动任务
                                                        .addTask(A0.class)
                                                        .addTask(B1.class)
                                                        .addTask(C2.class)
                                                        .addTask(D3.class)
                                                        .addTask(E4.class)
                                                        .addTask(F5.class)
                                                        // 添加启动监听
                                                        .addLaunchTaskListener(new ILaunchTaskListener() {
                                                            @Override
                                                            public void onTaskStarted(String taskName) {
                                                                System.out.println("onTaskStarted:" + taskName);
                                                            }
                                        
                                                            @Override
                                                            public void onTaskCompleted(String taskName) {
                                                                System.out.println("onTaskCompleted:" + taskName);
                                                            }
                                        
                                                            @Override
                                                            public void onAllTaskCompleted() {
                                                                System.out.println("onAllTaskCompleted");
                                                            }
                                                        })
                                                        // 开始运行所有任务
                                                        .start()
                                                        // 如果调用次方式，则当前调用的线程会阻塞，直到所有任务运行结束
                                                        .awaitAllTaskComplete();
                                2、组件化需要用到的注解
                ll_component_plugin：组件化的实现模块，通过一些简单注解，来实现轻量化组件化架构

            ---method_hook_plugin   可以实现方法拦截，方法开始和结束回调的插件，已经有思路，等后面再实现
            
            

         
