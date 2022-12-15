

package com.webank.weid.service.v2;

import com.webank.weid.blockchain.config.FiscoConfig;
import com.webank.weid.blockchain.constant.CnsType;
import com.webank.weid.blockchain.protocol.base.HashContract;
import com.webank.weid.blockchain.protocol.response.CnsInfo;
import com.webank.weid.blockchain.service.fisco.CryptoFisco;
import com.webank.weid.blockchain.util.WeIdUtils;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.contract.v2.DataBucket;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.service.CheckNodeFace;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.RespCallback;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.Response;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class CheckNodeServiceV2 implements CheckNodeFace {
    

    @Override
    public boolean check(FiscoConfig fiscoConfig) {
        try {
            BcosSDK bcosSDK = buildBcosSDK(fiscoConfig);
            return true;
        } catch (Exception e) {
            log.error("[check] check the node fail.", e);
            return false;
        }
    }
    

    private BcosSDK buildBcosSDK(FiscoConfig fiscoConfig) throws ConfigException {
        ConfigProperty configProperty = new ConfigProperty();

        List<String> nodeList = Arrays.asList(fiscoConfig.getNodes().split(","));
        Map<String, Object> netWork = new HashMap<String, Object>();
        netWork.put("peers", nodeList);
        log.info("[initNetWork] the current netWork: {}.", netWork);
        configProperty.setNetwork(netWork);

        Map<String, Object> cryptoMaterial = new HashMap<String, Object>();
        cryptoMaterial.put("useSMCrypto", fiscoConfig.getSdkSMCrypto());
        cryptoMaterial.put("certPath", fiscoConfig.getSdkCertPath());
//        cryptoMaterial.put("certPath", "D:\\projects\\weid\\WeIdentity\\out\\test\\resources");
//        cryptoMaterial.put("certPath", "D:\\projects\\weid\\WeIdentity\\out\\production\\resources");
        log.info("[initThreadPool] the cryptoMaterial: {}.", cryptoMaterial);
        configProperty.setCryptoMaterial(cryptoMaterial);

        BcosSDK bcosSdk = null;
        try {
            bcosSdk = new BcosSDK(new ConfigOption(configProperty));
            return bcosSdk;
        } catch (ConfigException ex) {
            log.error("init sdk instance failed", ex);
            throw ex;
        }
    }


    private Client getWeb3j(FiscoConfig fiscoConfig) throws Exception {
        BcosSDK bcosSDK = buildBcosSDK(fiscoConfig);
        Client client = bcosSDK.getClient(Integer.parseInt(fiscoConfig.getGroupId()));
        if (client == null) {
            log.error("[check] build the client fail.");
            throw new WeIdBaseException("init client fail or group not exist.");
        }
        return client;
    }

//
//    protected ThreadPoolTaskExecutor initializePool(FiscoConfig fiscoConfig) {
//        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
//        pool.setBeanName("web3sdk");
//        pool.setCorePoolSize(Integer.parseInt(fiscoConfig.getWeb3sdkCorePoolSize()));
//        pool.setMaxPoolSize(Integer.parseInt(fiscoConfig.getWeb3sdkMaxPoolSize()));
//        pool.setQueueCapacity(Integer.parseInt(fiscoConfig.getWeb3sdkQueueSize()));
//        pool.setKeepAliveSeconds(Integer.parseInt(fiscoConfig.getWeb3sdkKeepAliveSeconds()));
//        pool.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());
//        pool.initialize();
//        return pool;
//    }
    
    public int getBlockNumber(Client client) throws Exception {
        // callback
        CompletableFuture<BigInteger> future = new CompletableFuture<>();
        client.getBlockNumberAsync(new RespCallback<BlockNumber>() {
            @Override
            public void onResponse(BlockNumber blockNumber) {
                future.complete(blockNumber.getBlockNumber());
            }

            @Override
            public void onError(Response errorResponse) {
                future.complete(BigInteger.valueOf(-1L));
            }
        });
        try {
            BigInteger blockNumber = future.get(5000, TimeUnit.MILLISECONDS);
            return blockNumber.intValue();
        } catch (Exception ex) {
            throw new RuntimeException("get blockNumber failed after 5s wait");
        }
    }

    // 根据orgId获取org_config里面的hash数据.
    private HashContract getHashFromOrgCns(FiscoConfig fiscoConfig) throws Exception {
        List<HashContract> allHash = getAllHash(fiscoConfig);
        for (HashContract hashContract : allHash) {
            if (hashContract.getHash().equals(fiscoConfig.getCurrentOrgId())) {
                return hashContract;
            }
        }
        return null;
    }
    
    private DataBucket getDataBucket(FiscoConfig fiscoConfig) throws Exception {
        CryptoKeyPair credentials = CryptoFisco.cryptoSuite.getKeyPairFactory().generateKeyPair();
        Client client = getWeb3j(fiscoConfig);
        String contractAddress = getDataBucketAddress(client, credentials, CnsType.ORG_CONFING);
        if (StringUtils.isBlank(contractAddress)) {
            return null;
        }
        return DataBucket.load(
            contractAddress, 
            client, 
            credentials
        );
        //            finally {
//                client.stop();
//            }
    }
    
    // 获取所有的hash
    private List<HashContract> getAllHash(FiscoConfig fiscoConfig) throws Exception {
        int startIndex = 0;
        BigInteger num = BigInteger.valueOf(10);
        List<HashContract> hashContractList = new ArrayList<HashContract>();
        try {
            DataBucket dataBucket = getDataBucket(fiscoConfig);
            if (dataBucket == null) {
                return hashContractList;
            }
            while (true) {
                BigInteger offset = BigInteger.valueOf(startIndex);
                Tuple4<List<String>, List<String>, List<BigInteger>, BigInteger> data =
                    dataBucket.getAllBucket(offset, num);
                List<String> hashList = data.getValue1();
                List<String> ownerList = data.getValue2();
                List<BigInteger> timesList = data.getValue3();
                BigInteger next = data.getValue4();
                for (int i = 0; i < hashList.size(); i++) {
                    if (WeIdUtils.isEmptyStringAddress(ownerList.get(i))) {
                        break;
                    }
                    HashContract hash = new HashContract();
                    hash.setHash(hashList.get(i));
                    hash.setOwner(ownerList.get(i));
                    hash.setTime(timesList.get(i).longValue());
                    hashContractList.add(hash);
                }
                if (next.intValue() == 0) {
                    break;
                }
                startIndex = next.intValue();
            }
            log.info("[getAllHash] get the all hash success.");
            return hashContractList;
        } catch (Exception e) {
            log.error("[getAllHash] get the all hash fail.", e);
            throw e;
        }
    }
    
    // 判断是否存在机构配置
    @Override
    public boolean checkOrgId(FiscoConfig fiscoConfig) throws Exception {
        //从机构配置中获取当前机构的hash
        HashContract hashFromOrgIdCns = getHashFromOrgCns(fiscoConfig);
        // 如果不存在机构配置，则返回前端进行私钥配置
        if (hashFromOrgIdCns == null) {
            log.info("[checkOrgId] the orgId does not exist in orgConfig cns.");
            return false; //不存在
        }
        log.info("[checkOrgId] the orgId exist in orgConfig cns.");
        return true;
    }
    
    // 获取databucket地址
    private String getDataBucketAddress(Client client, CryptoKeyPair credentials, CnsType cnsType) {
        CnsInfo queryCnsInfo = queryCnsInfo(client, credentials, cnsType);
        if (queryCnsInfo != null) {
            return queryCnsInfo.getAddress();
        }
        return StringUtils.EMPTY;
    }
    
    // 查询cns地址
    private CnsInfo queryCnsInfo(Client client, CryptoKeyPair credentials, CnsType cnsType) throws WeIdBaseException {
        try {
            log.info("[queryBucketFromCns] query address by type = {}.", cnsType.getName());
            CnsService cnsService = new CnsService(client, credentials);
            List<org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo> cnsInfoListChain
                = cnsService.selectByName(cnsType.getName());
            List<CnsInfo> cnsInfoList = cnsInfoListChain.stream().map(CnsInfo::new).collect(
                Collectors.toList());
            if (!cnsInfoList.isEmpty()) {
                // 获取当前cnsType的大版本前缀
                String cnsTypeVersion = cnsType.getVersion();
                String preV = cnsTypeVersion.substring(0, cnsTypeVersion.indexOf(".") + 1);
                //从后往前找到相应大版本的数据
                for (int i = cnsInfoList.size() - 1; i >= 0; i--) {
                    CnsInfo cnsInfo = cnsInfoList.get(i);
                    if (cnsInfo.getVersion().startsWith(preV)) {
                        log.info("[queryBucketFromCns] query address form CNS successfully.");
                        return cnsInfo;
                    }
                }
            }
            log.warn("[queryBucketFromCns] can not find data from CNS.");
            return null;
        } catch (Exception e) {
            log.error("[queryBucketFromCns] query address has error.", e);
            throw new WeIdBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }
}
