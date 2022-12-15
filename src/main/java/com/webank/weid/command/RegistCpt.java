

package com.webank.weid.command;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.dto.CptFile;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.WeIdSdkService;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.rpc.WeIdService;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdSdkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author tonychen 2019/4/11
 */
@Slf4j
public class RegistCpt extends StaticConfig {

    private static WeIdSdkService weIdSdkService = new WeIdSdkService();

    private static WeIdService weIdService = new WeIdServiceImpl();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {

        if (args == null || args.length < 6) {
            log.error("[RegisterCpt] input parameters error, please check your input!");
            System.exit(1);
        }

        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);

        String weId = commandArgs.getWeid();
        
        ResponseData<Boolean> resp = weIdService.isWeIdExist(weId);
        if(!resp.getResult()) {
        	log.error("[RegisterCpt] weid ---> {} does not exist.", weId);
        	System.out.println("[RegisterCpt] Error: the WeId ---> " + weId + " does not exist.");
            System.exit(1);
        }
        
        String cptDir = commandArgs.getCptDir();
        String privateKeyFile = commandArgs.getPrivateKey();
        String cptId = commandArgs.getCptId();
        String cptFile = commandArgs.getCptFile();
        WeIdAuthentication weIdAuthentication = new WeIdAuthentication();
        weIdAuthentication.setWeId(weId);
        String osName = System.getProperty("os.name").toLowerCase();
        if(!privateKeyFile.startsWith("/") && !osName.contains("windows")) {
            //相对路径
            String temp = "tools/" + privateKeyFile;
            privateKeyFile = temp;
        }
        // 替换cnshash
        String mainHash = WeIdSdkUtils.getMainHash();
        privateKeyFile = privateKeyFile.replace("{cns_contract_follow}", mainHash);
        String privateKey = FileUtils.readFile(privateKeyFile);
        WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
        weIdPrivateKey.setPrivateKey(privateKey);
        weIdAuthentication.setWeIdPrivateKey(weIdPrivateKey);

        if (StringUtils.isNotEmpty(cptFile)) {
            File cptFile1 = new File(cptFile);
            try {
                registerCpt(cptFile1, weIdAuthentication, cptId);
            } catch (Exception e) {
                log.error("register Cpt has error.", e);
                System.exit(1);
            }
        }

        if (StringUtils.isNotEmpty(cptDir)) {
        	if(!cptDir.startsWith("/")) {
        		//相对路径
        		String temp = "tools/" + cptDir;
        		cptDir = temp;
        	}
            File file = new File(cptDir);
            if (!file.isDirectory()) {
                log.error("no CPT was found in dir :{}, please check your input.", file);
                System.out.println("[RegisterCpt] no CPT was found in dir :" + file + ", please check your input.");
                System.exit(1);
            }
            for (File f : file.listFiles()) {
                try {
                    registerCpt(f, weIdAuthentication, cptId);
                } catch (Exception e) {
                    log.error("register Cpt has error.", e);
                    System.exit(1);
                }
            }
        }
        System.exit(0);
    }
    
    private static void registerCpt(
        File cptFile,
        WeIdAuthentication callerAuth, 
        String cptId) throws IOException {

        ResponseData<CptFile> result = weIdSdkService.registerCpt(cptFile, callerAuth, cptId, DataFrom.COMMAND);
        System.out.print("[RegisterCpt] register cpt file:" + result.getResult());
        if (result.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            System.out.println("result ---> success. cpt id ---> " + result.getResult().getCptId());
        } else {
            System.out.println("result ---> fail. message ---> " + result.getErrorMessage());
        }
    }
}
