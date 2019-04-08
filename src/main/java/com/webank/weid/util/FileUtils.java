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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.FileOperator;

/**
 * @author tonychen 2019年4月8日
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
            ow.close();
        } catch (IOException e) {
            logger.error("writer file exception.", e);
        } finally {
            if (null != ow) {
                try {
                    ow.close();
                } catch (IOException e) {
                    logger.error("io close exception.", e);
                }
            }
        }
    }
}
