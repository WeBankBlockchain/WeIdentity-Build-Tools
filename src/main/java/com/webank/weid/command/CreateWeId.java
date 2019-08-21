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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.WeIdService;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.util.FileUtils;

/**
 * @author tonychen 2019/4/11
 */
public class CreateWeId {

    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(CreateWeId.class);

    private static WeIdService weIdService = new WeIdServiceImpl();

    /**
     * @param args
     */
    public static void main(String[] args) {

        ResponseData<CreateWeIdDataResult> response = weIdService.createWeId();
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error(
                "[CreateWeId] create WeID faild. error code : {}, error msg :{}",
                response.getErrorCode(),
                response.getErrorMessage());
            System.out.println("[CreateWeId] create WeID failed.");
            System.exit(1);
        }
        CreateWeIdDataResult result = response.getResult();
        String weId = result.getWeId();
        //System.out.println("weid is ------> " + weId);
        String publicKey = result.getUserWeIdPublicKey().getPublicKey();
        String privateKey = result.getUserWeIdPrivateKey().getPrivateKey();

        //write weId, publicKey and privateKey to output dir
        FileUtils.writeToFile(weId, "weid", FileOperator.OVERWRITE);
        FileUtils.writeToFile(publicKey, "ecdsa_key.pub", FileOperator.OVERWRITE);
        FileUtils.writeToFile(privateKey, "ecdsa_key", FileOperator.OVERWRITE);

        System.exit(0);
    }

}
