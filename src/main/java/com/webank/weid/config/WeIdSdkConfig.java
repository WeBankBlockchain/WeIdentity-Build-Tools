package com.webank.weid.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.webank.weid.rpc.AuthorityIssuerService;
import com.webank.weid.rpc.CptService;
import com.webank.weid.rpc.PolicyService;
import com.webank.weid.rpc.WeIdService;
import com.webank.weid.service.impl.AuthorityIssuerServiceImpl;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.service.impl.PolicyServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.util.PropertyUtils;

@Configuration
@Slf4j
public class WeIdSdkConfig {

//	@Bean
//	public FiscoConfig loadFiscoConfig() {
//		log.info("reload the properties...");
//		PropertyUtils.reload();
//		FiscoConfig fiscoConfig = new FiscoConfig();
//		fiscoConfig.load();
//		return fiscoConfig;
//	}

	@Bean
	public WeIdService getWeIdService() {
		return new WeIdServiceImpl();
	}

	@Bean
	public AuthorityIssuerService getAuthorityIssuerService() {
		return new AuthorityIssuerServiceImpl();
	}

	@Bean
	public CptService getCptService() {
		return new CptServiceImpl();
	}

	@Bean
	public PolicyService getPolicyService() {
		return new PolicyServiceImpl();
	}
}
