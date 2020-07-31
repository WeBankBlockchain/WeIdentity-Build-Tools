/*
 *       Copyright© (2018-2020) WeBank Co., Ltd.
 *
 *       This file is part of weidentity-build-tools.
 *
 *       weidentity-build-tools is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       weidentity-build-tools is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with weidentity-build-tools.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.webank.weid.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CompilerAndJarTools {
    
    private static final Logger logger = LoggerFactory.getLogger(CompilerAndJarTools.class);
    
    private static volatile JavaCompiler javaCompiler;
    
    private String javaSourcePath;
    
    private String javaClassPath;
    
    private String targetPath;
    
    CompilerAndJarTools(String javaSourcePath, String javaClassPath, String targetPath){
        this.javaSourcePath=javaSourcePath;
        this.javaClassPath=javaClassPath;
        this.targetPath = targetPath;
    }

    private static JavaCompiler getJavaCompiler() {
        if (javaCompiler == null) {
            synchronized (CompilerAndJarTools.class) {
                if (javaCompiler == null) {
                    javaCompiler = ToolProvider.getSystemJavaCompiler();
                }
            }
        }
        return javaCompiler;
    }
    
    public static CompilerAndJarTools instance(
        String javaSourcePath,
        String javaClassPath,
        String targetPath) {
        return new CompilerAndJarTools(javaSourcePath, javaClassPath, targetPath);
    }
    
    public CompilerAndJarTools complier() throws IOException {  
        logger.info("[complier] begin complier java source code.");
        File javaclassDir = new File(javaClassPath);  
        if (!javaclassDir.exists()) {  
            javaclassDir.mkdirs();  
        }  
        List<File> javaSourceList = new ArrayList<File>();  
        getFileList(new File(javaSourcePath), javaSourceList);  
        // 建立DiagnosticCollector对象
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        //该文件管理器实例的作用就是将我们需要动态编译的java源文件转换为getTask需要的编译单元
        StandardJavaFileManager fileManager = getJavaCompiler()
            .getStandardFileManager(diagnostics, null, null);
        // 获取要编译的编译单元
        Iterable<? extends JavaFileObject> compilationUnits = fileManager
            .getJavaFileObjectsFromFiles(javaSourceList);
        /**
         * 编译选项，在编译java文件时，编译程序会自动的去寻找java文件引用的其他的java源文件或者class。 
         * -sourcepath选项就是定义java源文件的查找目录，
         * -classpath选项就是定义class文件的查找目录，
         * -d就是编译文件的输出目录。
         */
        String jars = JsonInclude.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        Iterable<String> options = Arrays.asList(
            "-encoding","utf-8","-classpath",jars,"-d", javaClassPath, "-sourcepath", javaSourcePath);
        /**
         * 第一个参数为文件输出，这里我们可以不指定，我们采用javac命令的-d参数来指定class文件的生成目录
         * 第二个参数为文件管理器实例  fileManager
         * 第三个参数DiagnosticCollector<JavaFileObject> diagnostics是在编译出错时，存放编译错误信息
         * 第四个参数为编译命令选项，就是javac命令的可选项，这里我们主要使用了-d和-sourcepath这两个选项
         * 第五个参数为类名称
         * 第六个参数为上面提到的编译单元，就是我们需要编译的java源文件
         */
        JavaCompiler.CompilationTask task = getJavaCompiler().getTask(
                null,
                fileManager,
                diagnostics,
                options,
                null,
                compilationUnits);
        // 编译源程式
        boolean success = task.call();
        fileManager.close();
        if (success) {
            logger.info("[complier] complier successfully.");
        } else {
            logger.error("[complier] complier fail.");
            throw new RuntimeException("complier fail");
        }
        return this;
    }
    
    private void getFileList(File file, List<File> fileList) throws IOException {  
        if (file.isDirectory()) {  
            File[] files = file.listFiles();  
            for (int i = 0; i < files.length; i++) {  
                if (files[i].isDirectory()) {  
                    getFileList(files[i], fileList);  
                } else {  
                    fileList.add(files[i]);  
                }  
            }  
        }  
    }
    
    public void generateJar() throws FileNotFoundException, IOException {  
        logger.info("[generateJar] begin generate Jar");
        File file = new File(targetPath);
        createTempJar(javaClassPath, file.getParentFile().getAbsolutePath(), file.getName());
        logger.info("[generateJar] generate jar finish, file = {}", targetPath);
    }
    
    /**
     * @param rootPath    class文件根目录
     * @param targetPath  需要将jar存放的路径
     * @param jarFileName jar文件的名称
     * @exception IOException IO异常情况
     */
    public static void createTempJar(String rootPath, String targetPath, String jarFileName) throws IOException {
        if (!new File(rootPath).exists()) {
            throw new IOException(String.format("%s路径不存在", rootPath));
        }
        if (StringUtils.isBlank(jarFileName)) {
            throw new NullPointerException("jarFileName为空");
        }
        // 生成META-INF文件
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        // 创建临时jar
        File jarFile = File.createTempFile("edwin-", ".jar", new File(System.getProperty("java.io.tmpdir")));
        FileOutputStream fos = new FileOutputStream(jarFile);
        JarOutputStream out = new JarOutputStream(fos, manifest);
        createTempJarInner(out, new File(rootPath), "");
        out.flush();
        out.close();
        fos.close();
        // 生成目标路径
        File targetJarFile = new File(targetPath + File.separator + jarFileName);
        if (targetJarFile.exists() && targetJarFile.isFile())
            targetJarFile.delete();
        FileUtils.moveFile(jarFile, targetJarFile);
    }

    /**
     * @Description: 生成jar文件
     * @param out  文件输出流
     * @param f    文件临时File
     * @param base 文件基础包名
     * @return void
     * @Author zhangchengping
     * @Date 2019-06-07 00:02
     */
    private static void createTempJarInner(JarOutputStream out, File f, String base) throws IOException {

        if (f.isDirectory()) {
            File[] fl = f.listFiles();
            if (base.length() > 0) {
                base = base + "/";
            }
            for (int i = 0; i < fl.length; i++) {
                createTempJarInner(out, fl[i], base + fl[i].getName());
            }
        } else {
            out.putNextEntry(new JarEntry(base));
            FileInputStream in = new FileInputStream(f);
            byte[] buffer = new byte[1024];
            int n = in.read(buffer);
            while (n != -1) {
                out.write(buffer, 0, n);
                n = in.read(buffer);
            }
            in.close();
        }
    }
}
