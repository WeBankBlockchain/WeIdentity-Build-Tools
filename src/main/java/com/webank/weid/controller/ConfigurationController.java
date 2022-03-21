package com.webank.weid.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weid.app.BuildToolApplication;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.ConfigService;
import com.webank.weid.service.DataBaseService;
import com.webank.weid.service.GuideService;
import com.webank.weid.service.VerifyConfigService;
import com.webank.weid.service.WeIdSdkService;
import com.webank.weid.util.PropertyUtils;

/**
 * 配置管理
 */

@RestController
@RequestMapping(value = "/weid/weid-build-tools")
@Slf4j
public class ConfigurationController {

	private static boolean dbCheck = false;

	@Autowired
	private ConfigService configService;

	@Autowired
	private WeIdSdkService weIdSdkService;

	@Autowired
	private DataBaseService dataBaseService;

	@Autowired
	private VerifyConfigService verifyConfigService;

	@Autowired
	private GuideService guideService;

	@Description("加载run.config配置")
	@GetMapping("/loadConfig")
	public ResponseData<Map<String, String>> loadConfig() {
		log.info("start loadConfig.");
		return new ResponseData<>(configService.loadConfig(), ErrorCode.SUCCESS);
	}

	@Description("获取用户角色")
	@GetMapping("/getRole")
	public ResponseData<String> getRole() {
		log.info("start getRole.");
		return configService.getRoleType();
	}

	@Description("节点配置提交")
	@PostMapping("/nodeConfigUpload")
	public ResponseData<Boolean> nodeConfigUpload(HttpServletRequest request) {
		log.info("[nodeConfigUpload] begin upload file...");
		return configService.nodeConfigUpload(request);
	}

	@Description("提交群组Id")
	@PostMapping("/setGroupId")
	public ResponseData<Boolean> setMasterGroupId(@RequestParam("groupId") String groupId) {
		log.info("[setMasterGroupId] begin set the groupId = {}.", groupId);
		boolean result = configService.setMasterGroupId(groupId);
		PropertyUtils.reload();
		return new ResponseData<>(result, ErrorCode.SUCCESS);
	}

	@GetMapping("/dbCheckState")
	public ResponseData<Boolean> dbCheckState() {
		if (!dbCheck) {
			return checkPersistence();
		}
		return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
	}

	@Description("数据库检查")
	@GetMapping("/checkPersistence")
	public ResponseData<Boolean> checkPersistence() {
		dbCheck = false;
		if (!configService.isExistsForProperties()) {
			return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR);
		}
		log.info("begin check the persistence...");
		String persistence = configService.loadConfig().get("persistence_type");
		if (persistence.equals("mysql")) {
			dbCheck = configService.checkDb();
			if (dbCheck) {
				dataBaseService.initDataBase();
				return new ResponseData<>(dbCheck, ErrorCode.SUCCESS);
			}
		} else if (persistence.equals("redis")) {
			dbCheck = configService.checkRedis();
			return new ResponseData<>(dbCheck, ErrorCode.SUCCESS);
		}
		return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR);
	}

	@Description("数据库配置提交")
	@PostMapping("/submitDbConfig")
	public ResponseData<Boolean> submitDbConfig(HttpServletRequest request) {
		log.info("[submitDbConfig] begin submit dbconfig...");
		dbCheck = false;
		String persistenceType = request.getParameter("persistence_type");
		String mysqlAddress = request.getParameter("mysql_address");
		String database = request.getParameter("mysql_database");
		String username = request.getParameter("mysql_username");
		String mysqlPassword = request.getParameter("mysql_password");
		String redisAddress = request.getParameter("redis_address");
		String redisPassword = request.getParameter("redis_password");
		mysqlPassword = StringEscapeUtils.unescapeHtml(mysqlPassword);
		redisPassword = StringEscapeUtils.unescapeHtml(redisPassword);
		//根据模板生成配置文件
		if(configService.processDbConfig(persistenceType, mysqlAddress, database, username,
				mysqlPassword, redisAddress, redisPassword)) {
			return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
		}
		return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR);
	}

	@Description("数据库验证")
	@PostMapping("/verifyPersistence")
	public ResponseData<Boolean> verifyPersistence(@RequestParam("persistenceType") String persistenceType) {
		log.info("begin verify the persistence config = {}.", persistenceType);
		boolean result = verifyConfigService.verifyPersistence(persistenceType);
		PropertyUtils.reload();
		return new ResponseData<>(result, ErrorCode.SUCCESS);
	}

	@GetMapping("/persistenceCheckState")
	public ResponseData<Boolean> persistenceCheckState(HttpServletRequest request) {
		String persistenceType = configService.loadConfig().get("persistence_type");
		log.info("start persistence checkState, persistenceType:{}", persistenceType);
		if (!persistenceType.equals("mysql") && !persistenceType.equals("redis") ) {
			return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR.getCode(), "persistence type error");
		}
		return verifyPersistence(persistenceType);
	}

	@GetMapping("/checkState")
	public ResponseData<Map<String, Boolean>> checkState(HttpServletRequest request) {
		Map<String, Boolean> result = new HashMap<>();
		result.put("adminState", StringUtils.isNotBlank(guideService.checkAdmin()));
		result.put("nodeState", nodeCheckState().getResult());
		result.put("dbState", dbCheckState().getResult());
		result.put("groupState", groupCheckState().getResult());
		result.put("checkPersistenceState", persistenceCheckState(request).getResult());
		return new ResponseData<>(result, ErrorCode.SUCCESS);
	}

	@Description("节点检查")
	@GetMapping("/checkNode")
	public ResponseData<Boolean> checkNode() {
		log.info("start checkNode.");
		return configService.checkNode();
	}

	@GetMapping("/nodeCheckState")
	public ResponseData<Boolean> nodeCheckState() {
		return configService.nodeCheckState();
	}

	@GetMapping("/groupCheckState")
	public ResponseData<Boolean> groupCheckState() {
		String groupId = configService.loadConfig().get("group_id");
		return new ResponseData<>(weIdSdkService.getAllGroup(false).contains(groupId), ErrorCode.SUCCESS);
	}

	@RequestMapping("/refresh")
	public ResponseData<Boolean> restart() {
		try {
			ExecutorService threadPool = new ThreadPoolExecutor(1, 1, 0,
					TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.DiscardOldestPolicy());
			threadPool.execute(() -> {
				BuildToolApplication.context.close();
				BuildToolApplication.context = SpringApplication.run(BuildToolApplication.class,
						BuildToolApplication.args);
			});
			threadPool.shutdown();
			configService.setNodeCheck(false);
			dbCheck = false;
			return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
		} catch (Exception e) {
			log.error("[restart] the server restart fail.", e);
			return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR.getCode(), e.getMessage());
		}
	}
}
