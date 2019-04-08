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


package com.webank.weid.contract.transfer;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.protocol.base.Cpt;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.CptService;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.PropertyUtils;
import com.webank.weid.util.SerializationUtils;

/**
 * @author tonychen 2019年4月8日
 */
public class CptTools {

    private static final String CPT_KEY = "cpt.list";
    private static final Logger logger = LoggerFactory.getLogger(CptTools.class);
    private static CptService cptService = new CptServiceImpl();

    /**
     * @param args
     */
    public static void main(String[] args) {

        //1. get cpt list
        if (args == null || args.length < 1) {
            logger.error("[CptTools] input parameters error, please check your input!");
            System.exit(1);
        }
        String filePath = args[0];
        try {
            PropertyUtils.loadProperties(filePath);

            String cptStr = PropertyUtils.getProperty(CPT_KEY);
            String[] cptList = StringUtils.splitByWholeSeparator(cptStr, ",");

            //2. get cpt info from blockchain
            for (String cptId : cptList) {
                ResponseData<Cpt> response = cptService.queryCpt(Integer.valueOf(cptId));
                if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
                    logger
                        .error("Query CPT :{} failed. ErrorCode is:{}", cptId,
                            response.getErrorCode());
                    System.exit(1);
                }
                Cpt cpt = response.getResult();
                Map<String, Object> cptMap = cpt.getCptJsonSchema();
                String cptJson = SerializationUtils.serialize(cptMap);
                String fileName = "Cpt" + String.valueOf(cpt.getCptId()) + ".json";
                FileUtils.writeToFile(cptJson, fileName, FileOperator.OVERWRITE);
            }
        } catch (Exception e) {
            logger.error("[CptTools] execute with exception, {}", e);
            System.exit(1);
        }

        //3. exit with success.
        System.exit(0);
    }

}
