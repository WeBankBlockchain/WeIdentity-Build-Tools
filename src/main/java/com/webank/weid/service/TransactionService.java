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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.webank.weid.constant.AsyncStatus;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.SqlConstant;
import com.webank.weid.dto.AsyncInfo;
import com.webank.weid.dto.BinLog;
import com.webank.weid.dto.PageDto;
import com.webank.weid.protocol.request.TransactionArgs;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.util.JdbcHelper;
import com.webank.weid.util.OffLineBatchTask;

/**
 * 交易业务类
 * 1. rsync使用过程，命令方式还是java方式
 *   如果是命令方式： 
 *      1. 应用服务器如何执行脚本，
 *      2，脚本里面如何得到相关配置 如 ip，用户 密码，路径等
 *      3. 如果脚本放在classpath路径也行，那就要sdk定时调用shell脚本
 *   如果是java方式：rsync4j协议不兼容
 *  2. build-tool如何实现相关配置写入数据库，是通过命令还是页面，还是都兼容
 *     配置应该是做的run.config, 那一步做，还是单独做一步，比如是部署的时候配置入库，还是怎么处理
 *  3. 目前是否做补录，补录则要求是用可视化，
 * @author yanggang
 *
 */
@Service
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private static final int SEND_SUCCESS = 1;
    private static final int SEND_FAIL = 3;
    
    @Async
    public void reTrybatchTransaction(int time) {
        logger.info("[reTrybatchTransaction] batchTransaction by web");
        boolean result = batchTransaction(time, AsyncStatus.FAIL);
        logger.info("[reTrybatchTransaction] batchTransaction by web result = {}", result);
    }
    
    /**
    *  处理异步上链入口
    * @param time 定时任务传入当前时间，页面重试传入记录时间
    * @param asyncStatus 异步处理状态，定时任务传入0(目的是避免定时任务重复处理)， 页面重试传入3(失败)
    * @return 返回批量处理结果
    */
    public boolean batchTransaction(int time, AsyncStatus asyncStatus) {
        try {
            if (checkLock(time, asyncStatus)) {
                logger.info("[batchTransaction] begin do batchTransaction.");
                //锁定成功 进行批量上链操作
                AsyncInfo asyncInfo = null;
                try {
                    //查询异步处理记录
                    String selectSql = SqlConstant.DML_SELECT_ASYNC_INFO + " and data_time = ?";
                    asyncInfo = JdbcHelper.queryOne(selectSql, AsyncInfo.class, time);
                    batchTransaction(asyncInfo);
                    asyncInfo.setStatus(
                        asyncInfo.getFailSize() == 0 ? 
                        AsyncStatus.SUCCESS.getCode() : AsyncStatus.FAIL.getCode());
                } catch (Exception e) {
                    logger.error("[batchTransaction] do batchTransaction error.", e);
                    if (asyncInfo != null) {
                        asyncInfo.setStatus(AsyncStatus.FAIL.getCode());
                    }
                    return false;
                } finally {
                    //更新异步处理状态
                    if (asyncInfo != null) {
                        List<Object> paramters = buildUpdateAsync(asyncInfo);
                        JdbcHelper.execute(SqlConstant.DML_UPDATE_ASYNC_INFO, paramters.toArray());
                    }
                }
                return true;
            }
        } catch (Exception e) {
            logger.error("[batchTransaction] do batchTransaction error.", e);
        }
        return false;
    }
    
    private boolean checkLock(int time, AsyncStatus asyncStatus) {
        try {
            logger.info("[checkLock] begin check lock. time = {}, status = {}", time, asyncStatus);
            // 根据time查询是否有异步记录
            String selectSql = SqlConstant.DML_SELECT_ASYNC_INFO + " and data_time = ?";
            AsyncInfo asyncInfo = JdbcHelper.queryOne(selectSql, AsyncInfo.class, time);
            if (asyncInfo == null) {
                // 如果不存在异步记录，则新增异步记录
                try {
                    asyncInfo = buildAsyncInfo(time);
                    int allSize = JdbcHelper.queryCount(
                        SqlConstant.DML_SELECT_OFFLINE_TRANSACTION_COUNT, time);
                    asyncInfo.setAllSize(allSize);
                    List<Object> params = buildInsertAsync(asyncInfo);
                    JdbcHelper.execute(SqlConstant.DML_INSERT_ASYNC_INFO, params.toArray());
                    logger.info("[checkLock] lock success.");
                    //表示已锁定
                    return true;
                } catch (Exception e) {
                    //报错直接返回,无法获取锁定
                    logger.error("[checkLock] lock fail, insert async info error.", e);
                    return false;
                } 
            } else {
                // 如果存在异步记录则 则将状态更新为处理中.如果更新成功则执行下面操作
                int res = JdbcHelper.execute(
                    SqlConstant.DML_UPDATE_ASYNC_INFO_LOCK, asyncStatus.getCode(), time);
                if (res == 1) { //更新成功才有权限执行处理
                    //表示已锁定
                    logger.info("[checkLock] lock success.");
                    return true;
                }
                logger.info("[checkLock] lock fail, update status fail.");
                //锁定失败
                return false;
            }
        } catch (Exception e) {
            logger.error("[checkLock] lock fail, unkonw exception.", e);
            return false;
        }
    }
    
    private AsyncInfo buildAsyncInfo(int time) {
        AsyncInfo asyncInfo = new AsyncInfo();
        asyncInfo.setDataTime(time);
        asyncInfo.setAllSize(0);
        asyncInfo.setSuccessSize(0);
        asyncInfo.setFailSize(0);
        asyncInfo.setCreateTime(new Date());
        asyncInfo.setUpdateTime(asyncInfo.getCreateTime());
        asyncInfo.setStatus(1);//处理中
        return asyncInfo;
    }
    
    private List<Object> buildInsertAsync(AsyncInfo asyncInfo) {
        List<Object> list  = new  ArrayList<Object>();
        list.add(asyncInfo.getDataTime());
        list.add(asyncInfo.getStatus());
        list.add(asyncInfo.getAllSize());
        list.add(asyncInfo.getSuccessSize());
        list.add(asyncInfo.getFailSize());
        list.add(asyncInfo.getCreateTime());
        list.add(asyncInfo.getUpdateTime());
        return list;
    }
    
    private List<Object> buildUpdateAsync(AsyncInfo asyncInfo) {
        List<Object> list  = new  ArrayList<Object>();
        list.add(new Date());
        list.add(asyncInfo.getStatus());
        list.add(asyncInfo.getAllSize());
        list.add(asyncInfo.getSuccessSize());
        list.add(asyncInfo.getFailSize());
        list.add(asyncInfo.getDataTime());
        return list;
    }
    
    /**
     * 批量上链.
     */
    private void batchTransaction(AsyncInfo asyncInfo) {
        logger.info("begin send Transaction, time = {}.", asyncInfo.getDataTime());
        // 状态修改, 如异常情况，正常情况不存在这种异常
        fixedStatus(asyncInfo.getDataTime());
        // 查询当前需要处理上链的记录总数
        // 总失败记录数 = 总记录数 - 成功记录数
        int allFail = asyncInfo.getAllSize() - asyncInfo.getSuccessSize();
        int count = allFail / SqlConstant.BATCH_NUM + 1;
        AsyncInfo result = new AsyncInfo();
        for (int i = 0; i < count; i++) {
            try {
                // 1. 查询待交易上链记录1000条记录
                List<BinLog> binLogList = JdbcHelper.queryList(
                    SqlConstant.DML_SELECT_OFFLINE_TRANSACTION_FOR_SEND, 
                    BinLog.class,
                    asyncInfo.getDataTime());
                if (CollectionUtils.isEmpty(binLogList)) {
                    break;
                }
                result.setAllSize(result.getAllSize() + binLogList.size());
                // 2. 发送交易上链
                int[] sendResult = sendBatchTransaction(binLogList);
                // 3. 回写上链结果
                List<List<Object>> batchUpdateParams = buildBatchUpdateBinLog(
                    binLogList, sendResult);
                JdbcHelper.executeBatch(
                    SqlConstant.DML_UPDATE_OFFLINE_TRANSACTION_BY_ID, batchUpdateParams);
                for (int j : sendResult) {
                    if (j == SEND_SUCCESS) {
                        result.setSuccessSize(result.getSuccessSize() + 1);
                    } else {
                        result.setFailSize(result.getFailSize() + 1);
                    }
                }
            } catch (Exception e) {
                logger.error("batchTransaction has error.", e);
                break;
            } 
        }
        logger.info(
            "send Transacation end, all:{}, success:{}, fail:{}.", 
            result.getAllSize(), 
            result.getSuccessSize(), 
            result.getFailSize()
        );
        asyncInfo.setSuccessSize(asyncInfo.getSuccessSize() + result.getSuccessSize());
        asyncInfo.setFailSize(asyncInfo.getAllSize() - asyncInfo.getSuccessSize());
        // 将失败临时状态数据更新为上链失败
        fixedStatus(asyncInfo.getDataTime());
    }
    
    /*
     * 状态修正
     */
    private void fixedStatus(int time) {
        try {
            // 查询临时状态总数
            StringBuffer queryCount = new StringBuffer(
                SqlConstant.DML_SELECT_OFFLINE_TRANSACTION_COUNT);
            queryCount.append(" and status = 3 ");
            int all = JdbcHelper.queryCount(queryCount.toString(), time);
            // 以一次限制更新10000条数据为例，计算更新次数，避免大量更新问题
            int count = all / 10000 + 1;
            for (int i = 0; i < count; i++) {
                int execute = JdbcHelper.execute(
                    SqlConstant.DML_UPDATE_OFFLINE_TRANSACTION_FIXED_STATUS, time);
                if (execute == 0) {
                    break;
                }
            }
        } catch (SQLException e) {
            logger.error("[fixedStatus] fixed status error.");
        }
    }
    
    private List<List<Object>> buildBatchUpdateBinLog(List<BinLog> binLogList, int[] result) {
        List<List<Object>> objsList = new ArrayList<List<Object>>();
        for (int i = 0; i < binLogList.size(); i++) {
            BinLog binLog = binLogList.get(i);
            List<Object> paramList = new ArrayList<Object>();
            paramList.add(result[i]);
            paramList.add(new Date());
            paramList.add(binLog.getRequestId());
            objsList.add(paramList);
        }
        return objsList;
    }
    
    private int[] sendBatchTransaction(List<BinLog> binLogList) {
        logger.info("[sendBatchTransaction] begin sendBatchTransaction size: {}", binLogList.size());
        List<TransactionArgs> list = buildTransactionArgsList(binLogList);
        ResponseData<List<Boolean>> response = null;
        try {
            response = OffLineBatchTask.sendBatchTransaction(list);
            logger.info(
                "[sendBatchTransaction] sendBatchTransaction end and return： {} - {}.", 
                response.getErrorCode(), 
                response.getErrorMessage()
            );
        } catch (Throwable e) {
            logger.error("[sendBatchTransaction] has unkonw exception.", e);
            response = new ResponseData<List<Boolean>>(null, ErrorCode.UNKNOW_ERROR);
        }
        int[] result = new int[binLogList.size()];
        if (response.getErrorCode().intValue() != ErrorCode.SUCCESS.getCode()) {
            logger.error("[sendBatchTransaction] sendBatchTransaction fail. ");
            Arrays.fill(result, SEND_FAIL);
            return result;
        }
        List<Boolean> resList = response.getResult();
        for (int i = 0; i < resList.size(); i++) {
            if (resList.get(i)) {
                result[i] = SEND_SUCCESS;
            } else {
                result[i] = SEND_FAIL;
            }
        }
        return result;
    }
    
    private List<TransactionArgs> buildTransactionArgsList(List<BinLog> binLogList) {
        List<TransactionArgs> list = new ArrayList<TransactionArgs>();
        for (BinLog binLog : binLogList) {
            TransactionArgs args = new TransactionArgs();
            args.setArgs(binLog.getTransactionArgs());
            args.setMethod(binLog.getTransactionMethod());
            args.setBatch(String.valueOf(binLog.getBatch()));
            args.setExtra(binLog.getExtra());
            args.setRequestId(binLog.getRequestId());
            args.setTimeStamp(binLog.getTransactionTimestamp());
            list.add(args);
        }
        return list;
    }
    
    /**
     * 分页查询binLog记录,包括条件查询
     * @param pageDto 分页操作实体对象
     * @param binLog 查询条件实体
     */
    public void queryBinLogList(PageDto<BinLog> pageDto, BinLog binLog) {
        StringBuffer queryDataSql = new StringBuffer(
            SqlConstant.DML_SELECT_OFFLINE_TRANSACTION_FOR_PAGE);
        StringBuffer queryCountSql = new StringBuffer(
            SqlConstant.DML_SELECT_OFFLINE_TRANSACTION_COUNT);
        List<Object> params = new ArrayList<Object>();
        params.add(binLog.getBatch());
        StringBuffer where = new StringBuffer();
        if (binLog.getStatus() != -1) {
            where.append(" and status = ? ");
            params.add(binLog.getStatus());
        }
        try {
            queryCountSql.append(where.toString());
            int allCount = JdbcHelper.queryCount(queryCountSql.toString(), params.toArray());
            pageDto.setAllCount(allCount);
            
            params.add(pageDto.getStartIndex());
            params.add(pageDto.getPageSize());
            String sql = queryDataSql.toString().replace("$1", where.toString());
            List<BinLog> list = JdbcHelper.queryList(sql, BinLog.class, params.toArray());
            pageDto.setDataList(list);
        } catch (SQLException e) {
            logger.error("queryBinLogList fail.", e);
        }
    }
    
    /**
     * 分页查询异步记录
     * @param pageDto 分页操作实体
     * @param asyncInfo 查询条件实体
     */
    public void queryAsyncList(PageDto<AsyncInfo> pageDto, AsyncInfo asyncInfo) {
        StringBuffer queryData = new StringBuffer(SqlConstant.DML_SELECT_ASYNC_INFO);
        StringBuffer queryCount = new StringBuffer(SqlConstant.DML_SELECT_ASYNC_INFO_COUNT);
        List<Object> params = new ArrayList<Object>();
        StringBuffer where = new StringBuffer();
        if (asyncInfo.getStatus() != -1) {
            where.append(" and status = ? ");
            params.add(asyncInfo.getStatus());
        }
        if (asyncInfo.getDataTime() != 0) {
            where.append(" and data_time = ? ");
            params.add(asyncInfo.getDataTime());
        }
        
        try {
            queryCount.append(where.toString());
            int allCount = JdbcHelper.queryCount(queryCount.toString(), params.toArray());
            pageDto.setAllCount(allCount);
            
            where.append(" limit ?,? ");
            params.add(pageDto.getStartIndex());
            params.add(pageDto.getPageSize());
            queryData.append(where.toString());
            List<AsyncInfo> list = JdbcHelper.queryList(
                queryData.toString(), AsyncInfo.class, params.toArray());
            pageDto.setDataList(list);
        } catch (SQLException e) {
            logger.error("queryAsyncList fail.", e);
        }
    }
    
    /**
     * 页面重试
     * @param reqeustId 重试请求Id
     * @return 重试处理结果
     */
    public boolean reTryTransaction(int reqeustId) {
        // 根据id查询交易记录
        try {
            logger.info("begin retry to sendTransaction by reqeustId = {}.", reqeustId);
            BinLog binLog = JdbcHelper.queryOne(
                SqlConstant.DML_SELECT_OFFLINE_TRANSACTION_FOR_RETRY,BinLog.class, reqeustId);
            if (binLog == null) {
                return false;
            }
            // 获取锁，乐观锁，如果批次是失败状态，并且可以更新成功, 则获取锁
            if (checkLock(binLog.getBatch(), AsyncStatus.FAIL) ){
                // 发送交易上链
                List<BinLog> binLogList = new ArrayList<>();
                binLogList.add(binLog);
                int[] sendResult = sendBatchTransaction(binLogList);
                int status = sendResult[0];
                // 回写上链结果
                int count = JdbcHelper.execute(
                    SqlConstant.DML_UPDATE_OFFLINE_TRANSACTION_BY_ID, status, new Date(), reqeustId);
                logger.info("update status result = {}.", count);
                return count == 1;
            }
            return false;
        } catch (Exception e) {
            logger.error("reTryTransaction error.", e);
            return false;
        }
    }
}
