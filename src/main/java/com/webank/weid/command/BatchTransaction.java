package com.webank.weid.command;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.constant.AsyncStatus;
import com.webank.weid.service.TransactionService;
import com.webank.weid.util.DateFormatUtils;

/**
 * 异步上链入口
 * @author yanggang
 *
 */
public class BatchTransaction {
    
    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(BatchTransaction.class);
    
    //异步处理入口
    private static TransactionService transactionService = new  TransactionService();
    
    public static void main(String[] args) {
        try {
            //计算昨天时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1);
            String timeStr = DateFormatUtils.getDateString(calendar.getTime(), "yyyyMMdd");
            int time = Integer.parseInt(timeStr);
            logger.info("[BatchTransaction] beign process batchTransaction, time = {}", time);
            // 异步上链处理
            boolean result = transactionService.batchTransaction(time, AsyncStatus.INIT);
            if (result) {
                logger.info("[BatchTransaction] process successfully.");
                System.exit(1);
            } else {
                logger.error("[BatchTransaction] process fail.");
            }
        } catch (Exception e) {
            logger.error("[BatchTransaction] process has error.", e);
        }
        System.exit(0);
    }
}
