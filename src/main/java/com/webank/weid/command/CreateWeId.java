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

import lombok.extern.slf4j.Slf4j;

import com.webank.weid.config.StaticConfig;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.WeIdSdkService;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdUtils;

/**
 * @author tonychen 2019/4/11
 */
@Slf4j
public class CreateWeId extends StaticConfig {

    private static WeIdSdkService weIdSdkService = new WeIdSdkService();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {
        log.info("[CreateWeId] begin create weId.");
        ResponseData<String> responseData = weIdSdkService.createWeId(DataFrom.COMMAND);
        if (responseData.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
            System.out.println("[CreateWeId] create WeID failed, result: " + responseData.getErrorMessage());
            System.exit(1);
        }
        File weIdFile = weIdSdkService.getWeidDir(WeIdUtils.convertWeIdToAddress(responseData.getResult()));
        if (weIdFile.exists()) {
            FileUtils.writeToFile(responseData.getResult(), "weid");
            System.out.println("New weid has been created ---->" + responseData.getResult());
            System.out.println("The related private key and public key can be found at " 
                + weIdFile.getAbsolutePath());
        }
        System.exit(0);
    }

}
