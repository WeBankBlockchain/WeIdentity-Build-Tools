package com.webank.weid.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.beust.jcommander.JCommander;
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
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
        if (args == null || args.length < 4) {
            logger.error("[GeneratePolicy] input parameters error, please check your input!");
            System.exit(1);
        }
        
        CommandArgs commandArgs = new CommandArgs();
    	JCommander.newBuilder()
    	  .addObject(commandArgs)
    	  .build()
    	  .parse(args);
    	
        String cptStr = commandArgs.getCptIdList();
        String orgName = commandArgs.getOrgName();
      
        String[] cptList = StringUtils.splitByWholeSeparator(cptStr, ",");
        
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
        policyE.setOrgId(orgName);
        Map<String,String>extraMap = new HashMap<>();
        extraMap.put("extra1", "");
        extraMap.put("extra2", "");
        policyE.setExtra(extraMap);
        
        Map<String, Object> policyEMap;
		try {
			policyEMap = DataToolUtils.objToMap(policyE);
		
        Map<String, Object>policy1 = (HashMap<String, Object>)policyEMap.get("policy");
        for(Map.Entry<String, Object>entry:policy1.entrySet()) {
        	HashMap<String, Object> claimPolicyMap = (HashMap<String, Object>)entry.getValue();
        	HashMap<String, Object>disclosureMap =  DataToolUtils.deserialize((String)claimPolicyMap.get("fieldsToBeDisclosed"), HashMap.class);
        	claimPolicyMap.put("fieldsToBeDisclosed", disclosureMap);
        }
        String presentationPolicy = ConfigUtils.serialize(policyEMap);
        FileUtils.writeToFile(presentationPolicy, "presentation_policy.json", FileOperator.OVERWRITE);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[GeneratePolicy] generate policy failed. error message :{}", e);
		}
	}
	
	public static void buildInstance(Object obj) throws Exception {
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

    private static List<Object> buildList(Type type) throws Exception {
        ParameterizedType pt = (ParameterizedType) type;
        Type paType = pt.getActualTypeArguments()[0];
        // 如果又是一个List类型
        if (paType instanceof ParameterizedType) {
            ParameterizedType innerType = (ParameterizedType) (paType);
            List<Object> innerParaList = buildList(innerType);
            if (innerParaList != null) {
                List<Object> innerList = new ArrayList<>();
                innerList.add(innerParaList);
                return innerList;
            }
        } else { // 不是List
            String innerClassString = paType.getTypeName();
            Class<?> innerClass = Class.forName(innerClassString);
            // 如果是简单类型则不需要构建里面的对象
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
                generatePolicy((HashMap)value);
            }else if (value instanceof List) {
               boolean isMapOrList = generatePolicyFromList((ArrayList<Object>)value);
               if (!isMapOrList) {
                   entry.setValue(0);
               }
            } else {
                entry.setValue(0);
            }
        }
    }
    
    private static boolean generatePolicyFromList(List<Object> objList){
        List<Object> list = (List<Object>)objList;
        for (Object obj : list) {
            if (obj instanceof Map) {
                generatePolicy((HashMap)obj);
            } else if ( obj instanceof List) {
            	boolean result = generatePolicyFromList((ArrayList<Object>)obj);
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
