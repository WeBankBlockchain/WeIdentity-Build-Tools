

package com.webank.weid.service;

import com.webank.weid.blockchain.config.FiscoConfig;

public interface CheckNodeFace {

    boolean check(FiscoConfig fiscoConfig);
    
    boolean checkOrgId(FiscoConfig fiscoConfig) throws Exception;
}
