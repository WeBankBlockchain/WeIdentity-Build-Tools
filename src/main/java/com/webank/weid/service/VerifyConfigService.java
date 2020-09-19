package com.webank.weid.service;

import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.suite.api.persistence.PersistenceFactory;
import com.webank.weid.suite.api.persistence.inf.Persistence;
import com.webank.weid.suite.api.persistence.params.PersistenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VerifyConfigService {

    private static final Logger logger = LoggerFactory.getLogger(VerifyConfigService.class);

    public boolean verifyPersistence(String persistenceType) {
        logger.info("[verifyPersistence] begin verify persistence ...");
        PersistenceType type = null;
        Persistence persistence = null;
        String domain = "domain.defaultInfo";
        String id = "test";
        String data = "data123456";

        if (persistenceType.equals("mysql")) {
            type = PersistenceType.Mysql;
        } else if (persistenceType.equals("redis")) {
            type = PersistenceType.Redis;
        }
        persistence = PersistenceFactory.build(type);

        ResponseData<Integer> resAdd = persistence.add(domain, id, data);

        ResponseData<String> resGet = persistence.get(
                domain, id);
        ResponseData<Integer> resUpdate = persistence.update(
                domain, id, data + " update");
        ResponseData<Integer> resDelete = persistence.delete(domain, id);
        int addRes = resAdd.getErrorCode().intValue();
        int getRes = resGet.getErrorCode().intValue();
        int updateRes = resUpdate.getErrorCode().intValue();
        int deleteRes = resDelete.getErrorCode().intValue();
        if (ErrorCode.SUCCESS.getCode() == addRes && ErrorCode.SUCCESS.getCode() == getRes
                && ErrorCode.SUCCESS.getCode() == updateRes && ErrorCode.SUCCESS.getCode() == deleteRes) {
            return true;
        }
        return false;
    }
}
