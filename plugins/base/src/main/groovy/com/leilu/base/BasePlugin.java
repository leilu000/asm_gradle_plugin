package com.leilu.base;

import com.android.annotations.NonNull;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.ide.common.internal.WaitableExecutor;


import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public abstract class BasePlugin<T extends BaseExtensionInfo> extends Transform implements Plugin<Project> {

    private TransformOutputProvider mOutputProvider;
    protected T mExtension;
    protected boolean mIsAppModule;
    private boolean mIsIncremental;
    private final WaitableExecutor mExecuter = WaitableExecutor.useGlobalSharedThreadPool();
    private String mModuleName;

    @Override
    public void apply(Project project) {
        mModuleName = project.getName();
        BaseExtension extension = (BaseExtension) project.getExtensions().findByName("android");
        if (extension instanceof AppExtension
                || extension instanceof LibraryExtension) {
            mExtension = initSelfDefineExtension();
            if (mExtension == null) {
                throw new RuntimeException("The self define extension must be not null !");
            }
            String extensionName = mExtension.getName();
            if (extensionName == null || "".equals(extensionName)) {
                throw new RuntimeException("The self define extension name must be not null !");
            }
            mExtension = (T) project.getExtensions().create(extensionName, mExtension.getClass());

            mIsAppModule = extension instanceof AppExtension;
            extension.registerTransform(this);
        }
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        mIsIncremental = transformInvocation.isIncremental();
        printMsg("开始解析模块: " + mModuleName
                + " transform name:" + getName()
                + " 是否增量更新:" + mIsIncremental);
        mOutputProvider = transformInvocation.getOutputProvider();
        if (!mIsIncremental) {
            mOutputProvider.deleteAll();
        }
        transformInvocation.getInputs().forEach(transformInput -> {
            transformInput.getJarInputs().forEach(this::forEachJarInput);
            transformInput.getDirectoryInputs().forEach(this::forEachDirectoryInput);
        });
        mExecuter.waitForAllTasks();
        printMsg("解析" + mModuleName + "完成,transform name:" + getName()
                + "  耗时:" + (System.currentTimeMillis() - startTime));
    }

    private void forEachJarInput(JarInput jarInput) {
        mExecuter.execute(() -> {
            File file = jarInput.getFile();
            File destFile = getLocation(file.getAbsolutePath()
                    , jarInput.getContentTypes()
                    , jarInput.getScopes()
                    , Format.JAR);
            if (destFile.exists()) {
                FileUtil.deleteFile(destFile);
            }
            Status status = mIsIncremental ? jarInput.getStatus() : Status.ADDED;
            switch (status) {
                case ADDED:
                case CHANGED:
                    JarOutputStream jos = null;
                    JarFile jarFile = null;
                    try {
                        jos = new JarOutputStream(new FileOutputStream(destFile));
                        jarFile = new JarFile(file);
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry jarEntry = entries.nextElement();

                            byte[] srcData = FileUtil.readData(jarFile.getInputStream(jarEntry));
                            if (mExtension.enable && validateClass(jarEntry.getName())) {
                                printMsg("修改属于JarInput的类，当前module:" + mModuleName
                                        + "  类名:" + jarEntry.getName() + "  状态:" + status);
                                srcData = modifyJarInputClass(jarEntry.getName(), jos, srcData, status);
                            }
                            jos.putNextEntry(new JarEntry(jarEntry.getName()));
                            jos.write(srcData);
                            jos.flush();
                            jos.closeEntry();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        FileUtil.closeIO(jos);
                        FileUtil.closeIO(jarFile);

                    }
                    break;
                case REMOVED:
                    FileUtil.deleteFile(file);
                    break;
            }
            return null;
        });
    }

    private void forEachDirectoryInput(DirectoryInput directoryInput) {
        BiConsumer<File, Status> biConsumer = (file, status) -> {
            File destDir = getLocation(file.getName(), directoryInput.getContentTypes()
                    , directoryInput.getScopes(), Format.DIRECTORY);
            switch (status) {
                case ADDED:
                case CHANGED:
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    String srcPath = file.getAbsolutePath();
                    String destPath = destDir.getAbsolutePath() + File.separator + file.getName();
                    mExecuter.execute(() -> {
                        if (mExtension.enable && validateClass(file.getName())) {
                            printMsg("修改属于DirectoryInput的类，所在module:" + mModuleName
                                    + "  类名:" + file.getName() + "  状态:" + status);
                            byte[] srcData = FileUtil.readFile(srcPath);
                            byte[] classData = modifyDirectorInputClass(file.getName()
                                    , destDir.getAbsolutePath(), srcData, status);
                            FileUtil.saveFile(classData, destPath);
                        } else {
                            FileUtil.copyFile(file, destPath);
                        }
                        return null;
                    });
                    break;
                case REMOVED:
                    FileUtil.deleteFile(file);
                    break;
            }
        };
        if (mIsIncremental) {
            directoryInput.getChangedFiles().forEach(biConsumer);
        } else {
            FileUtil.getAllFile(directoryInput.getFile()).forEach(file -> {
                biConsumer.accept(file, Status.ADDED);
            });
        }
    }


    /**
     * 修改 JarInput下的class
     *
     * @param className  类名
     * @param jos        该class所在的jar包的输入流，如果有新增的class，需要将新增的class写入此流
     * @param sourceData 源字节码数据
     * @param status     状态
     * @return
     */
    protected abstract byte[] modifyJarInputClass(String className, JarOutputStream jos, byte[] sourceData, Status status);

    /**
     * 修改 DirectoryInput目录下的class
     *
     * @param className  类名
     * @param destDir    保存的类的目标文件夹
     * @param sourceData 源字节码数据
     * @param status     状态
     * @return
     */
    protected abstract byte[] modifyDirectorInputClass(String className, String destDir, byte[] sourceData, Status status);

    protected void printMsg(String msg) {
        if (mExtension != null && mExtension.showLog) {
            System.out.println(msg);
        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        if (mIsAppModule) {
            return TransformManager.SCOPE_FULL_PROJECT;
        }
        return TransformManager.PROJECT_ONLY;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    private File getLocation(String name, Set<QualifiedContent.ContentType> types,
                             @NonNull Set<? super QualifiedContent.Scope> scopes,
                             @NonNull Format format) {
        return mOutputProvider.getContentLocation(name, types, scopes, format);
    }

    private boolean validateClass(String name) {
        return name.endsWith(".class")
                && !name.contains("R$")
                && !name.contains("R.class")
                && !name.endsWith("BuildConfig.class")
                && !name.startsWith("android/support")
                && !name.startsWith("androidx/")
                && !name.startsWith("META-INF")
                && !name.startsWith("com/google/android/material")
                && !name.startsWith("kotlin/");
    }

    protected abstract T initSelfDefineExtension();

}
