

package com.webank.weid.service;

import com.webank.weid.constant.DataDriverConstant;
import com.webank.weid.constant.SqlConstant;
import com.webank.weid.suite.persistence.Persistence;
import com.webank.weid.suite.persistence.PersistenceFactory;
import com.webank.weid.suite.persistence.PersistenceType;
import com.webank.weid.suite.persistence.mysql.ConnectionPool;
import com.webank.weid.suite.persistence.mysql.SqlExecutor;
import com.webank.weid.util.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataBaseService {
    
    // sql执行器
    private static SqlExecutor sqlExecutor;
    
    private static final String CHECK_TABLE_SQL =
        "SELECT table_name "
        + DataDriverConstant.SQL_COLUMN_DATA
        + " FROM information_schema.TABLES "
        + " WHERE upper(table_name) = upper('$1')"
        + " and upper(table_schema) = upper('$2')";
    
    private static Persistence persistence;
    
    private Persistence getPersistence() {
        if (persistence == null) {
            String type = PropertyUtils.getProperty("persistence_type");
            PersistenceType persistenceType = null;
            if (type.equals("mysql")) {
                persistenceType = PersistenceType.Mysql;
            } else if (type.equals("redis")) {
                persistenceType = PersistenceType.Redis;
            }
            persistence = PersistenceFactory.build(persistenceType);
        }
        return persistence;
    }
    
    private SqlExecutor getSqlExecutor() {
        if (sqlExecutor == null) {
            sqlExecutor = new SqlExecutor(ConnectionPool.getFirstDataSourceName());
        }
        return sqlExecutor;
    }
    
    /**
     * 初始化表操作.
     */
    public void initDataBase() {
        initConfig();
        initTable();
    }
    
    //初始化系统配置
    private void initConfig() {
        //初始化Domain, 预留未来默认初始化系统配置
        getPersistence();
        log.info("[DataBaseService.initConfig] init successfully.");
    }
    //初始化自定义表
    private void initTable() {
        //初始化异步记录表
        initTable(SqlConstant.TABLE_NAME_ASYNC_INFO, SqlConstant.DDL_TABLE_NAME_ASYNC_INFO);
        initTable(
            SqlConstant.TABLE_NAME_OFFLINE_TRANSACTION_INFO, 
            SqlConstant.DDL_TABLE_NAME_OFFLINE_TRANSACTION_INFO
        );
    }
    
    private void initTable(String tableName, String createTableSql) {
        String checkTableSql = buildCheckSql(CHECK_TABLE_SQL, tableName);
        boolean result = getSqlExecutor().createTable(checkTableSql, createTableSql);
        if (result) {
            log.info("[DataBaseService.initTable] {} init successfully.", tableName);
        } else {
            log.error("[DataBaseService.initTable] {} init fail.", tableName);
        }
    }
    
    private String buildCheckSql(String exeSql, String tableName) {
        exeSql = exeSql.replace(SqlExecutor.TABLE_CHAR, tableName);
        return exeSql;
    }
}
