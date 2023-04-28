


package com.webank.weid.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.JCommander;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.dto.CptFile;
import com.webank.weid.protocol.base.PresentationPolicyE;
import com.webank.weid.blockchain.protocol.response.ResponseData;
import com.webank.weid.service.WeIdSdkService;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdSdkUtils;

/**
 * @author tonychen 2019/4/9
 */
@Slf4j
public class ToPojo {

    private static WeIdSdkService weIdSdkService = new WeIdSdkService();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {

        //1. get cpt list
        if (args == null || args.length < 2) {
            System.out.println("[CptToPojo] input parameters error, please check your input!");
            System.exit(1);
        }

        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);

        String cptStr = commandArgs.getCptIdList();
        String policyFile = commandArgs.getPolicyFileName();

        if (StringUtils.isEmpty(cptStr) && StringUtils.isEmpty(policyFile)) {
            System.out.println("[CptToPojo] input parameters error, please check your input!");
            System.exit(1);
        }

        if (StringUtils.isNotEmpty(cptStr) && StringUtils.isNotEmpty(policyFile)) {
            System.out.println("[CptToPojo] input parameters error, please check your input!");
            System.exit(1);
        }

        if (StringUtils.isNotEmpty(cptStr)) {
            createJarByCpt(cptStr);
        }
        if (StringUtils.isNotEmpty(policyFile)) {
            createJarByPolicy(policyFile);
        }

        //3. exit with success.
        System.exit(0);
    }

    private static void createJarByCpt(String cptStr) {

        List<Integer> succeedList = new ArrayList<>();
        List<Integer> failedList = new ArrayList<>();
        String pojoId = DataToolUtils.getUuId32();
        try {
            String[] cptIdStrs= StringUtils.splitByWholeSeparator(cptStr, ",");
            List<Integer> cptIdList = new ArrayList<>();
            //2. get cpt info from blockchain
            //根据cptId找registerId
            for (int i = 0; i < cptIdStrs.length; i++) {
                int cptId = Integer.parseInt(cptIdStrs[i]);
                File cptFile = weIdSdkService.getCptFile(cptId);
                if (!cptFile.exists()) {//如果找不到cpt文件
                    log.error(
                        "[CptToPojo] Error: the CPT ---> " + cptId + " does not exists. ");
                    failedList.add(cptId);
                    System.out.println(
                        "[CptToPojo] Error: the CPT ---> " + cptId + " does not exists. ");
                    continue;
                }
                weIdSdkService.generateJavaCodeByCpt(cptFile, cptId, pojoId, "cpt");
                cptIdList.add(cptId);
            }
            if (cptIdList.size() == 0) {
                System.out.println("[CptToPojo] no CPT founds.");
                System.exit(1);
            }
            Integer[] cptIds = cptIdList.toArray(new Integer[cptIdList.size()]);
            File sourceFile = WeIdSdkUtils.getSourceFile(pojoId);
            WeIdSdkUtils.createJar(sourceFile, cptIds, "cpt", DataFrom.COMMAND);
            succeedList.addAll(cptIdList);
            FileUtils.writeToFile(pojoId, "pojoId", FileOperator.OVERWRITE);
        } catch (Exception e) {
            log.error("[CptToPojo] execute with exception, {}", e);
            System.out.println("[CptToPojo] execute failed.");
            WeIdSdkUtils.deletePojoInfo(pojoId);
            System.exit(1);
        }

        if (CollectionUtils.isEmpty(failedList)) {
            System.out
                .println("[CptToPojo]All cpt ---> " + succeedList
                    + " are successfully transformed to pojo.");
        } else if (CollectionUtils.isEmpty(succeedList)) {
            System.out.println("[CptToPojo] Error: All cpt are failed to transformed to pojo.");
            System.exit(1);
        } else {
            System.out.println(
                "cpt:" + succeedList + " are successfully transformed to pojo, List:["
                    + failedList + "] are failed.");
        }
        System.out.println("The weidentity-cpt.jar can be found in:" + WeIdSdkUtils.getJarFile(pojoId).getAbsolutePath());
    }

    /**
     * convert policy to cpt pojo.
     *
     * @param policyFile the policy file
     */
    private static void createJarByPolicy(String policyFile) {
        try {
            String pojoId = DataToolUtils.getUuId32();
            PresentationPolicyE policyE = PresentationPolicyE.create(policyFile);
            if (policyE == null) {
                System.out.println("[policyToPojo] Presentation policy is null, illegal!");
                log.error("[policyToPojo] Presentation policy from file--->{} is null, illegal!",
                    policyFile);
                System.exit(1);
            }
            String policy = DataToolUtils.serialize(policyE);
            ResponseData<List<CptFile>> result =
                weIdSdkService.generateCptFileListByPolicy(policy);
            if (result.getErrorCode().intValue() != ErrorCode.SUCCESS.getCode()) {
                System.out.println("[policyToPojo] " + result.getErrorMessage());
                System.exit(1);
            }
            List<Integer> cptIdList = new ArrayList<>();
            for (CptFile cptFile : result.getResult()) {
                File file = new File(cptFile.getCptFileName());
                weIdSdkService.generateJavaCodeByCpt(file, cptFile.getCptId(), pojoId, "policy");
                cptIdList.add(cptFile.getCptId());
                //cpt生成代码文件后进行删除policy生成的中间cpt文件
                FileUtils.delete(file);
            }
            File sourceFile = WeIdSdkUtils.getSourceFile(pojoId);
            Integer[] cptIds = cptIdList.toArray(new Integer[cptIdList.size()]);
            WeIdSdkUtils.createJar(sourceFile, cptIds, "policy", DataFrom.COMMAND);
            System.out.println("Generate Pojo file by policy " + policyFile + " successfully.");
            System.out.println("The weidentity-cpt.jar can be found in:" + WeIdSdkUtils.getJarFile(pojoId).getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Generate Pojo by policy " + policyFile + " failed.");
            log.error("[CptToPojo] Generate Pojo by policy {} failed.", policyFile);
            System.exit(1);
        }
    }
}
