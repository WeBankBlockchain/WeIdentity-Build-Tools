package com.webank.weid.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.webank.weid.constant.FileOperator;
import com.webank.weid.protocol.base.ClaimPolicy;
import com.webank.weid.protocol.base.PresentationPolicyE;
import com.webank.weid.util.ConfigUtils;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;

/**
 * @author tonychen 2019年5月24日
 *
 */
public class GeneratePolicy {

	private static final Logger logger = LoggerFactory.getLogger(GeneratePolicy.class);
	
	private static final String CPT_KEY = "cpt.list";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
        if (args == null || args.length < 1) {
            logger.error("[GeneratePolicy] input parameters error, please check your input!");
            System.exit(1);
        }
        String filePath = args[0];
      

        try {
        	
        	ConfigUtils.loadProperties(filePath);
        }catch (Exception e) {
        	
        }

        String cptStr = ConfigUtils.getProperty(CPT_KEY);
        String[] cptList = StringUtils.splitByWholeSeparator(cptStr, ",");
//        String[] cptList = {"101","103"};
        
        PresentationPolicyE policyE = null ;
        Constructor<?>[] constructors = PresentationPolicyE.class.getDeclaredConstructors();
		for(Constructor constructor : constructors) {
			if(constructor.getParameterCount() == 0) {
				constructor.setAccessible(true);
				try {
					policyE = (PresentationPolicyE) constructor.newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
					return;
				}
				break;
			}
		}
        
        Map<Integer, ClaimPolicy> policy = new HashMap<>();
        for (String cptId : cptList) {
        	String className = "com.webank.weid.cpt."+"Cpt"+cptId;
        	try {
				Class clazz = Class.forName(className);
				Object obj = clazz.newInstance();
				buildInstance(obj);
				
				String s = DataToolUtils.serialize(obj);
				Map m = DataToolUtils.deserialize(s, HashMap.class);
				generatePolicy(m);
				String defaultDisclosure = DataToolUtils.serialize(m);
				ClaimPolicy claimPolicy = new ClaimPolicy();
				claimPolicy.setFieldsToBeDisclosed(defaultDisclosure);
				policy.put(Integer.valueOf(cptId), claimPolicy);
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("[GeneratePolicy] generate policy failed. error message :{}", e);
			} 
        }
        policyE.setPolicy(policy);
        
        Map<String, Object> policyEMap;
		try {
			policyEMap = DataToolUtils.objToMap(policyE);
		
        Map<String, Object>policy1 = (HashMap<String, Object>)policyEMap.get("policy");
        for(Map.Entry<String, Object>entry:policy1.entrySet()) {
//        	String cptIdKey = entry.getKey();
        	HashMap<String, Object> claimPolicyMap = (HashMap<String, Object>)entry.getValue();
        	HashMap<String, Object>disclosureMap =  DataToolUtils.deserialize((String)claimPolicyMap.get("fieldsToBeDisclosed"), HashMap.class);
        	claimPolicyMap.put("fieldsToBeDisclosed", disclosureMap);
        }
        String presentationPolicy = ConfigUtils.serialize(policyEMap);
        FileUtils.writeToFile(presentationPolicy, "presentationPolicy.json", FileOperator.OVERWRITE);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[GeneratePolicy] generate policy failed. error message :{}", e);
		}
	}
	
	public static void buildInstance(Object obj) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!BeanUtils.isSimpleValueType(field.getType())) {
                field.setAccessible(true);
                System.out.println(field.getType());
                Object inner_obj = field.getType().newInstance();
                buildInstance(inner_obj);
                field.set(obj, inner_obj);
            }
        }
    }
	
	private static void generatePolicy(Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map) {
				generatePolicy((HashMap) value);
			} else {
				String defaultValue = "0";
				entry.setValue(defaultValue);
			}
		}
	}

}
