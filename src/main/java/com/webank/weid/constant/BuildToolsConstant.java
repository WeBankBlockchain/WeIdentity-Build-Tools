/*
 *       Copyright© (2018) WeBank Co., Ltd.
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

package com.webank.weid.constant;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * The Class WeIdConstant.
 *
 * @author tonychen
 */
public final class BuildToolsConstant {

    /**
     * The Constant GAS_PRICE.
     */
    public static final BigInteger GAS_PRICE = new BigInteger("99999999999");

    /**
     * The Constant GAS_LIMIT.
     */
    public static final BigInteger GAS_LIMIT = new BigInteger("9999999999999");

    /**
     * The Constant INIIIAL_VALUE.
     */
    public static final BigInteger INILITIAL_VALUE = new BigInteger("0");

    /**
     * UTF-8.
     */
    public static final String UTF_8 = "UTF-8";
    
    public static final String CPT_JAR_NAME = "weidentity-cpt.jar";
    
    public static final String SUCCESS = "success";
    
    public static final String FAIL = "fail";
    
    public static final String APPLY_NAME = "applyName";
    
    public static final String EVIDENCE_NAME = "evidenceName";
    
    public static final List<Integer> CPTID_LIST = Arrays.asList(11, 101, 103, 105, 106, 107, 108, 109, 110, 111);

    public static final String WEID_FILE = "weid";
    public static final String ECDSA_KEY = "ecdsa_key";
    public static final String ECDSA_PUB_KEY = "ecdsa_key.pub";
    public static final String WEID_PATH = "output/create_weid";
    public static final String ISSUER_TYPE_PATH = "output/issuer_type";
    public static final String CPT_PATH = "output/cpt";
    public static final String CPT_RESULT_PATH = "output/regist_cpt";
    public static final String POJO_PATH = "output/pojo";
    public static final String PROPERTIES_KEY = "properties";
    public static final String ITEMS_KEY = "items";
    public static final String CLAIM_KEY = "claim";

    public static final String HASH = "hash";
    public static final String ROLE_FILE = "role";
    public static final String GUIDE_FILE = "guide";
    public static final String WEBASE_FILE = "webase";
    public static final String ADMIN_PATH = "output/admin";
    public static final String DEPLOY_PATH = "output/deploy";
    public static final String SHARE_PATH = "output/share";
    public static final String OTHER_PATH = "output/other";
    public static final String RESOURCES_PATH = "resources/";
    public static final String CONTRACT_PATH = "contract";
    

    public static final String AUTH_ADDRESS_FILE_NAME = "authorityIssuer.address";
    public static final String CPT_ADDRESS_FILE_NAME = "cptController.address";
    public static final String WEID_ADDRESS_FILE_NAME = "weIdContract.address";
    public static final String EVID_ADDRESS_FILE_NAME = "evidenceController.address";
    public static final String SPECIFIC_ADDRESS_FILE_NAME = "specificIssuer.address";

    public static final String RUN_CONFIG_BAK = "output/.run.config";
}
