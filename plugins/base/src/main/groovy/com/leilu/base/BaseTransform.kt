package com.leilu.base

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.builder.utils.isValidZipEntryName
import com.android.utils.FileUtils
import com.google.common.io.Files
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * @author: created by leilu
 * email: lu.lei@hsbc.com
 *
 */
abstract class BaseTransform : Transform() {

    override fun isIncremental(): Boolean = false

    abstract fun doAsmAction(inputStream: InputStream, outputStream: OutputStream)

    open fun classFilter(className: String) = className.endsWith(SdkConstants.DOT_CLASS)

    override fun transform(transformInvocation: TransformInvocation) {
        log("start $name incremental: ${transformInvocation.isIncremental}")

        val inputProvider = transformInvocation.inputs
        val outputProvider = transformInvocation.outputProvider

        if (!transformInvocation.isIncremental) {
            log("All File deleted.")
            outputProvider.deleteAll()
        }

        for (input in inputProvider) {
            log("Transform jarInputs start.")
            for (jarInput in input.jarInputs) {
                val inputJar = jarInput.file
                val outputJar = outputProvider.getContentLocation(
                    jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR
                )
                if (transformInvocation.isIncremental) {
                    // 3.1 Transform jar input in incremental build.
                    when (jarInput.status ?: Status.NOTCHANGED) {
                        Status.NOTCHANGED -> {
                            // Do nothing.
                        }
                        Status.ADDED, Status.CHANGED -> {
                            // Do transform.
                            transformJar(inputJar, outputJar, this::doAsmAction)
                        }
                        Status.REMOVED -> {
                            // Delete.
                            FileUtils.delete(outputJar)
                        }
                    }
                } else {
                    // 3.2 Transform jar input in full build.
                    transformJar(inputJar, outputJar, this::doAsmAction)
                }
            }
            // 4. Transform dir input.
            log("Transform dirInput start.")
            for (dirInput in input.directoryInputs) {
                val inputDir = dirInput.file
                val outputDir = outputProvider.getContentLocation(
                    dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY
                )
                if (transformInvocation.isIncremental) {
                    // 4.1 Transform dir input in incremental build.
                    for ((inputFile, status) in dirInput.changedFiles) {
                        val outputFile = concatOutputFilePath(outputDir, inputFile)
                        when (status ?: Status.NOTCHANGED) {
                            Status.NOTCHANGED -> {
                                // Do nothing.
                            }
                            Status.ADDED, Status.CHANGED -> {
                                // Do transform.
                                doTransformFile(inputFile, outputFile)
                            }
                            Status.REMOVED -> {
                                // Delete
                                FileUtils.delete(outputFile)
                            }
                        }
                    }
                } else {
                    // 4.2 Transform dir input in full build.
                    // Traversal fileTree (depthFirstPreOrder).
                    for (inputFile in FileUtils.getAllFiles(inputDir)) {
                        val outputFile = concatOutputFilePath(outputDir, inputFile)
                        if (classFilter(inputFile.name)) {
                            log("transform filename: ${inputFile.name}")
                            doTransformFile(inputFile, outputFile)
                        } else {
                            // Copy.
                            Files.createParentDirs(outputFile)
                            FileUtils.copyFile(inputFile, outputFile)
                        }
                    }
                }
            }
        }
        log("Transform end.")
    }

    protected fun log(msg: String) {
        println("************** $msg **************")
    }

    /**
     * Do transform Jar.
     */
    private fun transformJar(
        inputJar: File, outputJar: File, function: ((InputStream, OutputStream) -> Unit)?
    ) {
        // Create parent directories to hold outputJar file.
        Files.createParentDirs(outputJar)
        // Unzip.
        FileInputStream(inputJar).use { fis ->
            ZipInputStream(fis).use { zis ->
                // Zip.
                FileOutputStream(outputJar).use { fos ->
                    ZipOutputStream(fos).use { zos ->
                        var entry = zis.nextEntry
                        while (entry != null && isValidZipEntryName(entry)) {
                            if (!entry.isDirectory) {
                                zos.putNextEntry(ZipEntry(entry.name))
                                if (classFilter(entry.name)) {
                                    log("transform jar file,jar: ${inputJar.name}  filename: ${entry.name}")
                                    applyFunction(zis, zos, function)
                                } else {
                                    // Copy.
                                    zis.copyTo(zos)
                                }
                            }
                            entry = zis.nextEntry
                        }
                    }
                }
            }
        }
    }

    /**
     * Do transform file.
     */
    private fun doTransformFile(
        inputFile: File, outputFile: File
    ) {
        // Create parent directories to hold outputFile file.
        Files.createParentDirs(outputFile)
        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                // Apply transform function.
                applyFunction(fis, fos, this::doAsmAction)
            }
        }
    }

    private fun concatOutputFilePath(outputDir: File, inputFile: File) =
        File(outputDir, inputFile.name)

    private fun applyFunction(
        input: InputStream, output: OutputStream, function: ((InputStream, OutputStream) -> Unit)?
    ) {
        try {
            if (null != function) {
                function.invoke(input, output)
            } else {
                // Copy
                input.copyTo(output)
            }
        } catch (e: Exception) {
            throw e.cause!!
        }
    }

}