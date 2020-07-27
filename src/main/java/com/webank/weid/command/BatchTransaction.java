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

package com.webank.weid.command;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.constant.AsyncStatus;
import com.webank.weid.service.TransactionService;
import com.webank.weid.util.DateFormatUtils;

/**
 * 异步上链入口
 * @author yanggang
 *
 */
public class BatchTransaction extends StaticConfig {
    
    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(BatchTransaction.class);
    
    //异步处理入口
    private static TransactionService transactionService = new  TransactionService();
    
    public static void main(String[] args) {
        try {
            CommandArgs commandArgs = new CommandArgs();
            JCommander.newBuilder()
                .addObject(commandArgs)
                .build()
                .parse(args);
            //计算昨天时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1);
            String timeStr = DateFormatUtils.getDateString(calendar.getTime(), "yyyyMMdd");
            int time = Integer.parseInt(timeStr);
            AsyncStatus asyncStatus = AsyncStatus.INIT;
            // 如果输入了时间
            if (StringUtils.isNotBlank(commandArgs.getDataTime())) {
                int inputTime = Integer.parseInt(commandArgs.getDataTime());
                // 时间不能大于今天
                if (inputTime > time) {
                    log("[BatchTransaction] input time error, time needs to be less than " + time);
                    System.exit(1);
                }
                time = inputTime;
                // 如果输入了状态
                if (StringUtils.isNotBlank(commandArgs.getAsyncStatus())) {
                    int status = Integer.parseInt(commandArgs.getAsyncStatus());
                    if (status == AsyncStatus.INIT.getCode()) {
                        // 初始状态
                        asyncStatus = AsyncStatus.INIT;
                    } else if (status == AsyncStatus.FAIL.getCode()) {
                        // 失败状态
                        asyncStatus = AsyncStatus.FAIL;
                    } else {
                        // 非初始和失败状态
                        log("[BatchTransaction] input status error, status needs 0 or 3 ");
                        System.exit(1);
                    }
                }
            }
            log("[BatchTransaction] beign process batchTransaction, time = " + time);
            // 异步上链处理
            boolean result = transactionService.batchTransaction(time, asyncStatus);
            if (result) {
                log("[BatchTransaction] process successfully.");
                System.exit(0);
            } else {
                log("[BatchTransaction] process fail.");
            }
        } catch (Exception e) {
            String message = "[BatchTransaction] process has error.";
            logger.error(message, e);
            System.out.println(message);
        }
        System.exit(1);
    }
    private static void log(String message) {
        logger.info(message);
        System.out.println(message);
    }
}
