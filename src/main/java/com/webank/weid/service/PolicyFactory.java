/*
 *       Copyright© (2018-2020) WeBank Co., Ltd.
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

package com.webank.weid.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

 import com.webank.weid.protocol.base.ClaimPolicy;
import com.webank.weid.protocol.base.PresentationPolicyE;
import com.webank.weid.util.ClassUtils;
import com.webank.weid.util.ConfigUtils;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.PropertyUtils;

public class PolicyFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(PolicyFactory.class);
    
    private ClassLoader classLoader;
    
    public PolicyFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public static PolicyFactory loadJar(String jarPath) throws Exception {
        return new PolicyFactory(ClassUtils.loadJar(jarPath));
    }
    
    public String generate(String cptStr, String policyType, String policyId, String fromType) throws Exception {
        
        String[] cptList = StringUtils.splitByWholeSeparator(cptStr, ",");
        PresentationPolicyE policyE = null;
        Constructor<?>[] constructors = PresentationPolicyE.class.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                try {
                    policyE = (PresentationPolicyE) constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                    logger.error("[generate] create instance for PresentationPolicyE has error.", e);
                    return StringUtils.EMPTY;
                }
                break;
            }
        }
        if (policyE == null) {
            logger.error("[generate] can not create instance for PresentationPolicyE");
            return StringUtils.EMPTY;
        }
        
        boolean isZkp = false;
        policyE.setVersion(1);
        if (!"original".equals(policyType)) {
            policyType = "zkp";
            isZkp = true;
            policyE.setVersion(2);
        }
        
        Map<Integer, ClaimPolicy> policy = new HashMap<>();
        for (String cptId : cptList) {
            String className = "com.weidentity.weid.cpt"+ cptId;
            if ("policy".equals(fromType)) {
                className = className + ".policy";
            }
            className = className + "." + "Cpt" + cptId; 
            try {
                Class<?> clazz = classLoader.loadClass(className);
                
                Object obj = clazz.newInstance();
                buildInstance(obj);

                String s = DataToolUtils.serialize(obj);
                Map m = DataToolUtils.deserialize(s, HashMap.class);
                generatePolicy(m);
                Map claimPolicyMap = new LinkedHashMap<String, Object>();
                if (isZkp) {
                    claimPolicyMap.put("claim", m);
                    claimPolicyMap.put("id", 0);
                    claimPolicyMap.put("cptId", 0);
                    claimPolicyMap.put("context", 0);
                    claimPolicyMap.put("issuer", 0);
                    claimPolicyMap.put("issuanceDate", 0);
                    claimPolicyMap.put("expirationDate", 0);
                } else {
                    claimPolicyMap.putAll(m);
                }
                String defaultDisclosure = DataToolUtils.serialize(claimPolicyMap);
                ClaimPolicy claimPolicy = new ClaimPolicy();
                claimPolicy.setFieldsToBeDisclosed(defaultDisclosure);
                policy.put(Integer.valueOf(cptId), claimPolicy);

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("[GeneratePolicy] generate policy failed. error message :{}", e);
            }
        }
        policyE.setPolicy(policy);
        String currentOrgId = PropertyUtils.getProperty("blockchain.orgid");
        policyE.setOrgId(currentOrgId);
        if (StringUtils.isNotEmpty(policyId)) {
            policyE.setId(Integer.valueOf(policyId));
        }

        try {
            LinkedHashMap<String, Object> policyEMap = 
                ConfigUtils.objToMap(policyE, LinkedHashMap.class);
            Map<String, Object> policy1 = (HashMap<String, Object>) policyEMap.get("policy");
            policyEMap.put("policyType", policyType);
            for (Map.Entry<String, Object> entry : policy1.entrySet()) {
                HashMap<String, Object> claimPolicyMap = (HashMap<String, Object>) entry.getValue();
                HashMap<String, Object> disclosureMap = DataToolUtils
                    .deserialize((String) claimPolicyMap.get("fieldsToBeDisclosed"), LinkedHashMap.class);
                claimPolicyMap.put("fieldsToBeDisclosed", disclosureMap);
            }
            Map<String, String> extraMap = new HashMap<>();
            extraMap.put("extra1", "");
            extraMap.put("extra2", "");
            policyEMap.put("extra", extraMap);
            
            String presentationPolicy = ConfigUtils.serialize(policyEMap);
            return presentationPolicy;
        } catch (Exception e) {
            logger.error("[GeneratePolicy] generate policy failed. error message :{}", e);
            throw e;
        }
    }
    
    private void buildInstance(Object obj) throws Exception {
        Class<?> cls = obj.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (isSimpleValueType(field.getType())) {
                field.setAccessible(true);
                if (field.getType() == List.class) {
                    Type type = field.getGenericType();
                    List<Object> list = buildList(type);
                    if (list != null) {
                        field.set(obj, list);
                    }
                } else {
                    Object inner_obj = field.getType().newInstance();
                    buildInstance(inner_obj);
                    field.set(obj, inner_obj);
                }
            }
        }
    }

    private List<Object> buildList(Type type) throws Exception {
        ParameterizedType pt = (ParameterizedType) type;
        Type paType = pt.getActualTypeArguments()[0];
        // if list
        if (paType instanceof ParameterizedType) {
            ParameterizedType innerType = (ParameterizedType) (paType);
            List<Object> innerParaList = buildList(innerType);
            if (innerParaList != null) {
                List<Object> innerList = new ArrayList<>();
                innerList.add(innerParaList);
                return innerList;
            }
        } else {
            //not list
            String innerClassString = paType.getTypeName();
            Class<?> innerClass = classLoader.loadClass(innerClassString);
            if (isSimpleValueType(innerClass)) {
                Object inner = innerClass.newInstance();
                buildInstance(inner);
                List<Object> innerList = new ArrayList<>();
                innerList.add(inner);
                return innerList;
            }
        }
        return null;
    }

    private static boolean isSimpleValueType(Class<?> cls) {
        return !BeanUtils.isSimpleValueType(cls) && !cls.isArray();
    }

    private static void generatePolicy(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                generatePolicy((HashMap) value);
            } else if (value instanceof List) {
                boolean isMapOrList = generatePolicyFromList((ArrayList<Object>) value);
                if (!isMapOrList) {
                    entry.setValue(0);
                }
            } else {
                entry.setValue(0);
            }
        }
    }

    private static boolean generatePolicyFromList(List<Object> objList) {
        List<Object> list = (List<Object>) objList;
        for (Object obj : list) {
            if (obj instanceof Map) {
                generatePolicy((HashMap) obj);
            } else if (obj instanceof List) {
                boolean result = generatePolicyFromList((ArrayList<Object>) obj);
                if (!result) {
                    return result;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
