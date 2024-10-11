package com.webank.weid.controller;

import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.ParamKeyConstant;
import com.webank.weid.dto.AsyncInfo;
import com.webank.weid.dto.BinLog;
import com.webank.weid.dto.PageDto;
import com.webank.weid.kit.amop.inner.PropertiesService;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 异步上链管理
 */

@RestController
@RequestMapping(value = "/weid/weid-build-tools/")
@Slf4j
public class AsyncUpChainController {

	@Autowired
	private TransactionService transactionService;

	@Description("查询异步记录列表")
	@PostMapping("/getAsyncList")
	public ResponseData<PageDto<AsyncInfo>> getAsyncList(
			@RequestParam(value = "dataTime") int dataTime,
			@RequestParam(value = "status") int status,
			@RequestParam(value = "iDisplayStart") int iDisplayStart,
			@RequestParam(value = "iDisplayLength") int iDisplayLength
	) {
		log.info("start getAsyncList, dataTime:{}, status:{}, iDisplayStart:{}, iDisplayLength:{}",
				dataTime, status, iDisplayStart, iDisplayLength);
		return transactionService.queryAsyncList(dataTime, status, iDisplayStart, iDisplayLength);
	}

	@Description("异步任务重试")
	@PostMapping("/reTryAsyn")
	public ResponseData<Boolean> reTryAsyn(@RequestParam(value = "dataTime") int dataTime) {
		log.info("start reTry asyn, dataTime:{}", dataTime);
		// 异步调度
		transactionService.reTrybatchTransaction(dataTime);
		return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
	}

	@Description("检查是否已开启异步上链")
	@PostMapping("/chekEnableAsync")
	@Deprecated
	public ResponseData<Boolean> checkEnableAsync() {
		log.info("start check enable async");
		String offLine = PropertiesService.getInstance().getProperty(ParamKeyConstant.ENABLE_OFFLINE);
		if (StringUtils.isBlank(offLine)) {
			return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR);
		}
		return new ResponseData<>(Boolean.valueOf(offLine), ErrorCode.SUCCESS);
	}

	@Description("修改异步上链状态")
	@PostMapping("/doEnableAsync")
	@Deprecated
	public ResponseData<Boolean> doEnableAsync(@RequestParam(value = "enable") boolean enable) {
		log.info("start doEnableAsync, enable:{}", enable);
		Map<String, String> map = new HashMap<>();
		map.put(ParamKeyConstant.ENABLE_OFFLINE, String.valueOf(enable));
		PropertiesService.getInstance().saveProperties(map);
		return checkEnableAsync();
	}

	@Description("查询交易列表")
	@PostMapping("/getBinLogList")
	public ResponseData<PageDto<BinLog>> getBinLogList(
			@RequestParam(value = "batch") int batch,
			@RequestParam(value = "status") int status,
			@RequestParam(value = "iDisplayStart") int iDisplayStart,
			@RequestParam(value = "iDisplayLength") int iDisplayLength
	) {
		log.info("start getBinLogList, batch:{}, status:{}, iDisplayStart:{}, iDisplayLength:{}",
				batch, status, iDisplayStart, iDisplayLength);
		return transactionService.queryBinLogList(batch, status, iDisplayStart, iDisplayLength);
	}

	@Description("单条binlog重试, 暂时不做单条重试，因为涉及比较复杂，需要考虑多人同时重试，异步处理正在重试")
	//@PostMapping("/reTryTransaction")
	public ResponseData<Boolean> reTryTransaction(
			@RequestParam(value = "requestId") int requestId
	) {
		return transactionService.reTryTransaction(requestId);
	}
}
