

package com.webank.weid.service;

import com.webank.weid.config.FiscoConfig;

public interface CheckNodeFace {

    boolean check(FiscoConfig fiscoConfig);
    
    boolean checkOrgId(FiscoConfig fiscoConfig) throws Exception;
}
