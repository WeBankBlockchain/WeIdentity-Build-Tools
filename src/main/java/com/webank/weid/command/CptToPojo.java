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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.JCommander;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.protocol.base.Cpt;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.CptService;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;

/**
 * @author tonychen 2019/4/9
 */
public class CptToPojo {

    private static final Logger logger = LoggerFactory.getLogger(CptToPojo.class);
    private static CptService cptService = new CptServiceImpl();

    /**
     * @param args
     */
    public static void main(String[] args) {

        //1. get cpt list
        if (args == null || args.length < 2) {
            System.out.println("[CptTools] input parameters error, please check your input!");
            System.exit(1);
        }

        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);

        String cptStr = commandArgs.getCptIdList();
        if (StringUtils.isEmpty(cptStr)) {
            System.out.println("[CptTools] input parameters error, please check your input!");
            System.exit(1);
        }

        List<String> succeedList = new ArrayList<>();
        List<String> failedList = new ArrayList<>();
        try {

            String[] cptList = StringUtils.splitByWholeSeparator(cptStr, ",");
            //2. get cpt info from blockchain
            for (String cptId : cptList) {
                ResponseData<Cpt> response = cptService.queryCpt(Integer.valueOf(cptId));
                if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
                    logger.error("Query CPT :{} failed. ErrorCode is:{},ErrorMessage:{}", cptId,
                        response.getErrorCode(), response.getErrorMessage());
                    failedList.add(cptId);
                    System.out.println("[CptToPojo] Error: get CPT ---> "+cptId+" failed. reason is ---> "+ response.getErrorMessage());
                    continue;
                }
                Cpt cpt = response.getResult();
                Map<String, Object> cptMap = cpt.getCptJsonSchema();
                String cptJson = DataToolUtils.serialize(cptMap);
                String fileName = "Cpt" + String.valueOf(cpt.getCptId()) + ".json";
                FileUtils.writeToFile(cptJson, fileName, FileOperator.OVERWRITE);
                succeedList.add(String.valueOf(cpt.getCptId()));
            }
        } catch (Exception e) {
            logger.error("[CptToPojo] execute with exception, {}", e);
            System.out.println("[CptToPojo] execute failed.");
            System.exit(1);
        }

        if (CollectionUtils.isEmpty(failedList)) {
            System.out
                .println("[CptToPojo]All cpt ---> " + succeedList + " are successfully transformed to pojo.");
        } else if(CollectionUtils.isEmpty(succeedList)) {
        	System.out.println("[CptToPojo] Error: All cpt are failed to transformed to pojo.");
        	System.exit(1);
        } else {
            System.out.println(
                "cpt:" + succeedList + " are successfully transformed to pojo, List:["
                    + failedList + "] are failed.");
        }

        //3. exit with success.
        System.exit(0);
    }

}
