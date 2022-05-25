package com.leilu.base;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class FileUtil {

    public static void addClassToJarFile(JarOutputStream jos, String className, byte[] classData) {
        JarEntry jarEntry = new JarEntry(className);
        try {
            jos.putNextEntry(jarEntry);
            jos.write(classData);
            jos.flush();
            jos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readData(InputStream is) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = 0;
        byte[] buffer = new byte[1024];
        try {
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(bos);
        }
        return null;
    }

    public static void saveFile(byte[] data, String classPath) {
        FileOutputStream fos = null;
        try {
            File file = new File(classPath);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(classPath);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeIO(fos);
        }
    }

    public static byte[] readFile(String classPath) {
        FileInputStream fos = null;
        ByteArrayOutputStream bos = null;
        try {
            fos = new FileInputStream(classPath);
            bos = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fos.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            fos.close();
            bos.close();
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(fos);
            closeIO(bos);
        }
        return null;
    }

    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFile(f);
                }
            }
        }
        file.deleteOnExit();
    }

    public static void copyDir(File dir, String dest) {
        if (dir.isFile()) {
            copyFile(dir, dest);
            return;
        }
        File destDir = new File(dest);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                copyDir(file, dest);
                continue;
            }
            copyFile(file, dest + File.separator + file.getName());
        }
    }

    public static List<File> getAllFile(File file) {
        List<File> list = new ArrayList<>();
        depthEachFile(list, file);
        return list;
    }

    private static void depthEachFile(List<File> list, File file) {
        if (file.isFile()) {
            list.add(file);
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    depthEachFile(list, f);
                }
            }
        }
    }

    public static void copyFile(File src, String dest) {
        if (!src.exists()) {
            return;
        }
        OutputStream os = null;
        InputStream is = null;
        try {
            File file = new File(dest);
            if (!file.exists()) {
                file.createNewFile();
            }
            os = new FileOutputStream(dest);
            is = new FileInputStream(src);
            byte[] buffer = new byte[2046];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeIO(os);
            closeIO(is);
        }
    }

    public static void closeIO(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
