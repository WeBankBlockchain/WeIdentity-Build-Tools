/*
 *       Copyright© (2018-2019) WeBank Co., Ltd.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.FileOperator;

/**
 * @author tonychen 2019/4/8
 */
public final class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static void writeToFile(
        String content,
        String fileName,
        FileOperator operator) {

        OutputStreamWriter ow = null;
        try {
            boolean flag = true;
            File file = new File(fileName);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            if (StringUtils.equals(operator.getAction(), FileOperator.OVERWRITE.getAction()) && file
                .exists()) {
                flag = file.delete();
            }
            if (!flag) {
                logger.error("writeAddressToFile() delete file is fail.");
                return;
            }
            ow = new OutputStreamWriter(new FileOutputStream(fileName, true),
                BuildToolsConstant.UTF_8);
            ow.write(content);
            ow.flush();
        } catch (IOException e) {
            logger.error("writer file exception.", e);
        } finally {
            close(ow);
        }
    }

    public static void writeToFile(
            String content,
            String fileName) {
        writeToFile(content, fileName, FileOperator.OVERWRITE);
    }
    
    public static String readFile(String fileName) {

        File file = new File(fileName);
        if (!file.exists()) {
            return StringUtils.EMPTY;
        }
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] data = new byte[in.available()];
            int length = in.read(data);
            if (length < 0) {
                return StringUtils.EMPTY;
            }
            return new String(data, 0, length);
        } catch (FileNotFoundException e) {
            logger.error("the file can not found.", e);
        } catch (IOException e) {
            logger.error("read file exception.", e);
        } finally {
            close(in);
        }
        return StringUtils.EMPTY;
    }
    
    public static List<String> readFileToList(String fileName) {

        File file = new File(fileName);
        List<String> data = new ArrayList<String>();
        if (!file.exists()) {
            return data;
        }
        InputStream in = null;
        BufferedReader reader = null;
        try {
            in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line = reader.readLine()) != null) {
                data.add(line);
            }
        } catch (FileNotFoundException e) {
            logger.error("the file can not found.", e);
        } catch (IOException e) {
            logger.error("read file exception.", e);
        } finally {
            close(reader);
            close(in);
        }
        return data;
    }
    
    public static boolean exists(String fileName) {
       File file = new File(fileName);
       return file.exists();
    }
    
    public static void copy(File srcFile, File targetFile) {
        //创建输入输出流
        InputStream in =  null;
        OutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(targetFile);
            byte[] bytes = new byte[1024];
            int len = -1;
            while((len = in.read(bytes))!=-1) {
                out.write(bytes,0,len);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            logger.error("the file can not found.", e);
        } catch (IOException e) {
            logger.error("copy file exception.", e);
        } finally {
            close(in);
            close(out);
        }
    }
    
    public static void close(InputStream is) {
       if (is != null) {
            try {
               is.close();
            } catch (IOException e) {
                logger.error("io close exception.", e);
            }
        }
    }
    
    public static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                logger.error("io close exception.", e);
            }
        }
    }
    
    public static void close(Writer ow) {
        if (ow != null) {
            try {
                ow.close();
            } catch (IOException e) {
                logger.error("io close exception.", e);
            }
        }
    }
    
    public static void close(Reader or) {
        if (or != null) {
            try {
                or.close();
            } catch (IOException e) {
                logger.error("io close exception.", e);
            }
        }
    }
    
    public static void mkdirs(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    
    public static void deleteAll(File file) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                deleteAll(childFile);
            }
        }
        delete(file);
    }
    
    public static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
    
    public static void delete(String filePath) {
        delete(new File(filePath));
    }
}
