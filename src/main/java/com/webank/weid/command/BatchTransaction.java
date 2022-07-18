

package com.webank.weid.command;

import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
@Slf4j
public class BatchTransaction extends StaticConfig {
    
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
            log.error(message, e);
            System.out.println(message);
        }
        System.exit(1);
    }
    private static void log(String message) {
        log.info(message);
        System.out.println(message);
    }
}
