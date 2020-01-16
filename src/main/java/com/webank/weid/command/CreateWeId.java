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

package com.webank.weid.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.constant.DataFrom;
import com.webank.weid.service.BuildToolService;
import com.webank.weid.util.WeIdUtils;

/**
 * @author tonychen 2019/4/11
 */
public class CreateWeId {

    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(CreateWeId.class);

    private static BuildToolService buildToolService = new BuildToolService();

    /**
     * @param args
     */
    public static void main(String[] args) {
        logger.info("[CreateWeId] begin create weId.");
        String weId = buildToolService.createWeId(DataFrom.COMMAND);
        File weIdFile = buildToolService.getWeidDir(WeIdUtils.convertWeIdToAddress(weId));
        if (weIdFile.exists()) {
            System.out.println("New weid has been created ---->" + weId);
            System.out.println("The related private key and public key can be found at " 
                + weIdFile.getAbsolutePath());
        }
        System.exit(0);
    }

}
