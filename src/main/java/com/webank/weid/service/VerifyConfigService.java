package com.webank.weid.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.suite.api.persistence.PersistenceFactory;
import com.webank.weid.suite.api.persistence.inf.Persistence;
import com.webank.weid.suite.api.persistence.params.PersistenceType;

@Service
@Slf4j
public class VerifyConfigService {
    
    public boolean verifyPersistence(String persistenceType) {
        log.info("[verifyPersistence] begin verify persistence ...");
        PersistenceType type = null;
        String domain = "domain.defaultInfo";
        String id = "test";
        String data = "data123456";

        if (persistenceType.equals("mysql")) {
            type = PersistenceType.Mysql;
        } else if (persistenceType.equals("redis")) {
            type = PersistenceType.Redis;
        }
        Persistence persistence = PersistenceFactory.build(type);

        ResponseData<Integer> resAdd = persistence.add(domain, id, data);

        ResponseData<String> resGet = persistence.get(
                domain, id);
        ResponseData<Integer> resUpdate = persistence.update(
                domain, id, data + " update");
        ResponseData<Integer> resDelete = persistence.delete(domain, id);
        int addRes = resAdd.getErrorCode();
        int getRes = resGet.getErrorCode();
        int updateRes = resUpdate.getErrorCode();
        int deleteRes = resDelete.getErrorCode();
        return ErrorCode.SUCCESS.getCode() == addRes && ErrorCode.SUCCESS.getCode() == getRes
                && ErrorCode.SUCCESS.getCode() == updateRes && ErrorCode.SUCCESS.getCode() == deleteRes;
    }
}
