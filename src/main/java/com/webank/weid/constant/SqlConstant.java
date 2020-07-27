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

package com.webank.weid.constant;

/**
 * SQL常量类
 * @author yanggang
 *
 */
public class SqlConstant {
    
    public static final int BATCH_NUM = 1000;
    
    /* 异步上链处理记录表 */
    public static final String TABLE_NAME_ASYNC_INFO = "weidentity_async_info";
    /* 异步上链处理记录表建表语句 */
    public static final String DDL_TABLE_NAME_ASYNC_INFO = 
        " CREATE TABLE " + TABLE_NAME_ASYNC_INFO + " ( "
        +  "  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',"
        +  "  data_time int(8) NOT NULL COMMENT '处理时间',"
        +  "  status char(1) NOT NULL COMMENT '处理状态，0: 待处理,1:处理中,2:处理成功,3:处理失败', "
        +  "  all_size int(8) NOT NULL COMMENT '总记录数', "
        +  "  success_size int(8) NOT NULL COMMENT '成功记录数', "
        +  "  fail_size int(8) NOT NULL COMMENT '失败记录数', "
        +  "  create_time datetime NOT NULL COMMENT '创建时间', "
        +  "  update_time datetime NOT NULL COMMENT '更新时间', "
        +  "  PRIMARY KEY (id), "
        +  "  UNIQUE KEY uq_wbi_rid (data_time) "
        +  " ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='异步上链处理表' ";
    /* 写入异步上链记录 */
    public static final String DML_INSERT_ASYNC_INFO = "insert into " + TABLE_NAME_ASYNC_INFO
        + " (data_time, status, all_size, success_size, fail_size, create_time, update_time) "
        + " values (?, ?, ?, ?, ?, ?, ?) ";
    /* 根据时间查询异步记录 */
    public static final String DML_SELECT_ASYNC_INFO= "select "
        + " id, data_time, status, all_size, success_size, fail_size, create_time, update_time "
        + " from " + TABLE_NAME_ASYNC_INFO + " where 1 = 1 ";
    /* 查询总记录数 */
    public static final String DML_SELECT_ASYNC_INFO_COUNT = "select count(1) data"
        + " from " + TABLE_NAME_ASYNC_INFO + " where 1 = 1 ";
    /* 更新异步记录表*/
    public static final String DML_UPDATE_ASYNC_INFO = "update " + TABLE_NAME_ASYNC_INFO 
        + " set update_time = ?, status = ?, all_size = ?, success_size = ?, fail_size = ? "
        + " where data_time = ? ";
    /* 利用乐观锁进行抢占*/
    public static final String DML_UPDATE_ASYNC_INFO_LOCK = "update " + TABLE_NAME_ASYNC_INFO
        + " set status = 1 where status = ? and data_time = ? ";
    
    /* 离线交易记录表 */
    public static final String TABLE_NAME_OFFLINE_TRANSACTION_INFO = "weidentity_offline_transaction_info";
    /* 离线交易记录表表建表语句 */
    public static final String DDL_TABLE_NAME_OFFLINE_TRANSACTION_INFO = 
        " CREATE TABLE " + TABLE_NAME_OFFLINE_TRANSACTION_INFO +" ( "
        + "  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID', "
        + "  request_id varchar(100) NOT NULL COMMENT '交易请求Id', "
        + "  transaction_method varchar(50) NOT NULL COMMENT '交易方法', "
        + "  transaction_args varchar(4000) NOT NULL COMMENT '交易参数', "
        + "  transaction_timestamp bigint(20) NOT NULL COMMENT '发送交易的时间戳', "
        + "  extra varchar(4000) DEFAULT NULL COMMENT '交易扩展信息', "
        + "  batch int(8) NOT NULL COMMENT '批次', "
        + "  status char(1) NOT NULL DEFAULT '0' COMMENT '交易状态,0未上链，初始状态，1上链成功，2上链失败', "
        + "  created datetime DEFAULT NULL COMMENT '创建时间', "
        + "  updated datetime DEFAULT NULL COMMENT '更新时间', "
        + "  PRIMARY KEY (id), "
        + "  UNIQUE KEY uq_woti_rid (request_id), "
        + "  key idx_woti_btsi (batch, status, id) "
        + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='WeIdentity离线交易记录表'; ";
    
    // 查询时间范围内待上链的数据(包括带上链状态数据 和 上链失败数据)
    public static final String DML_SELECT_OFFLINE_TRANSACTION_FOR_SEND = " select "
        + " id, request_id, transaction_method, transaction_args, transaction_timestamp,"
        + " extra, batch, status, created, updated "
        + " from " + TABLE_NAME_OFFLINE_TRANSACTION_INFO
        + " where batch = ? and status in (0, 2) limit " + BATCH_NUM;
    
    public static final String DML_SELECT_OFFLINE_TRANSACTION_FOR_RETRY = " select "
        + " id, request_id, transaction_method, transaction_args, transaction_timestamp,"
        + " extra, batch, status, created, updated "
        + " from " + TABLE_NAME_OFFLINE_TRANSACTION_INFO + " where request_id = ? status = 2";

    public static final String DML_SELECT_OFFLINE_TRANSACTION_FOR_PAGE = " select "
            + " t.id, request_id, transaction_method, transaction_args, transaction_timestamp,"
            + " extra, batch, status, created, updated"
            + " from " + TABLE_NAME_OFFLINE_TRANSACTION_INFO + " t "
            + " INNER JOIN ("
            + " SELECT id FROM " + TABLE_NAME_OFFLINE_TRANSACTION_INFO + " where batch = ? $1 "
            + " LIMIT ?,?"
            + " ) AS i ON t.id = i.id";
    
    public static final String DML_SELECT_OFFLINE_TRANSACTION_COUNT = " select count(1) data from "
        + TABLE_NAME_OFFLINE_TRANSACTION_INFO +" where batch = ? ";
    
    public static final String DML_UPDATE_OFFLINE_TRANSACTION_BY_ID = " update "
        + TABLE_NAME_OFFLINE_TRANSACTION_INFO
        + " set status = ?, updated = ? where request_id = ? and status in (0, 2) ";
    
    // 避免出现一次更新大量数据情况，此处限制一次最多更新10000条记录
    public static final String DML_UPDATE_OFFLINE_TRANSACTION_FIXED_STATUS = " update "
        + TABLE_NAME_OFFLINE_TRANSACTION_INFO 
        + " set status = 2 where batch = ? and status = 3 limit 10000";
    
    
}
