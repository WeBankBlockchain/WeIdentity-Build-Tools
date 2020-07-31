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

import com.beust.jcommander.JCommander;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.service.BuildToolService;
import com.webank.weid.service.PolicyFactory;
import com.webank.weid.util.FileUtils;

/**
 * @author tonychen 2019/5/24
 */
public class GeneratePolicy {

    private static final Logger logger = LoggerFactory.getLogger(GeneratePolicy.class);
    
    private static BuildToolService buildToolService = new BuildToolService();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {

        if (args == null || args.length < 4) {
            System.err.println("[GeneratePolicy] input parameters error, please check your input!");
            System.exit(1);
        }

        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);

        String cptStr = commandArgs.getCptIdList();
        String policyId = commandArgs.getPolicyId();
        String pojoId = commandArgs.getPojoId();
        String policyType = commandArgs.getType();//original，zkp
        
        try {
            File file = buildToolService.getJarFile(pojoId);
            if (!file.exists()) {
                System.err.println("[GeneratePolicy] the pojo does not exists.");
                System.exit(1);
            }
            String policy = PolicyFactory.loadJar(file.getAbsolutePath())
                .generate(cptStr, policyType, policyId, "cpt");
            FileUtils.writeToFile(policy, "presentation_policy.json",
                FileOperator.OVERWRITE);
            System.exit(0);
        } catch (Exception e) {
            logger.error("[GeneratePolicy] generate policy has error.", e);
            System.out.println("generate policy fail, please check the log.");
            System.exit(1);
        }
    }
}
