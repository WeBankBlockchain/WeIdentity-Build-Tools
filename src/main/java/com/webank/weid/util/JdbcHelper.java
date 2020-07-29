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

package com.webank.weid.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weid.suite.persistence.sql.ConnectionPool;
   
/** 
 * 数据库访问帮助类 
 *  
 * @author WANGYAN 
 *  
 */  
public class JdbcHelper {
    
    private static final String dsName = ConnectionPool.getFirstDataSourceName(); 
    
    public static List<Map<String, Object>> queryList(String sql, Object... paramters)  
            throws SQLException {  
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionPool.getConnection(dsName);
            preparedStatement = conn.prepareStatement(sql);
            setParams(preparedStatement, paramters);
            rs = preparedStatement.executeQuery();
            return ResultToListMap(rs);
        } catch (SQLException e) {  
            throw new SQLException(e);
        } finally {  
            ConnectionPool.close(conn, preparedStatement, rs);
        }
    }
    
    public static <T> List<T> queryList(String sql, Class<T> clz, Object... paramters)  
            throws SQLException {  
        List<Map<String, Object>> queryList = queryList(sql, paramters);
        return JdbcHelper.convertToList(queryList, clz);
    }
    
    /*
     * 查询记录总数
     */
    public static int queryCount(String sql, Object... paramters)  
        throws SQLException {  
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionPool.getConnection(dsName);
            preparedStatement = conn.prepareStatement(sql);
            setParams(preparedStatement, paramters);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return Integer.parseInt(String.valueOf(rs.getObject(1)));
            }
            return 0;
        } catch (SQLException e) {  
            throw new SQLException(e);
        } finally {  
            ConnectionPool.close(conn, preparedStatement, rs);
        }
    }
    
    public static Map<String, Object> queryOne(String sql, Object... paramters)  
        throws SQLException {  
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionPool.getConnection(dsName);
            preparedStatement = conn.prepareStatement(sql);
            setParams(preparedStatement, paramters);
            rs = preparedStatement.executeQuery();
            return ResultToMap(rs);
        } catch (SQLException e) {  
            throw new SQLException(e);
        } finally {  
            ConnectionPool.close(conn, preparedStatement, rs);
        }
    }
    
    public static <T> T queryOne(String sql, Class<T> clz, Object... paramters)  
        throws SQLException {  
        Map<String, Object> queryOne = queryOne(sql, paramters);
        return JdbcHelper.convertToObj(queryOne, clz);
    }
    
    private static List<Map<String, Object>> ResultToListMap(ResultSet rs) throws SQLException {  
        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()) {  
            Map<String, Object> map = extractedMap(rs);
            list.add(map);
        }
        return list;
    }

    private static Map<String, Object> extractedMap(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        Map<String, Object> map = new HashMap<>();
        for (int i = 1; i <= md.getColumnCount(); i++) {  
            map.put(md.getColumnLabel(i), rs.getObject(i));
        }
        return map;
    }
    
    private static Map<String, Object> ResultToMap(ResultSet rs) throws SQLException {  
        if (rs.next()) {  
            return extractedMap(rs);
        }
        return null;
    }
      
    public static int execute(String sql, Object... paramters) throws SQLException {  
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {  
            conn = ConnectionPool.getConnection(dsName);
            preparedStatement = conn.prepareStatement(sql);
            setParams(preparedStatement, paramters);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {  
            throw new SQLException(e);
        } finally {
            ConnectionPool.close(conn, preparedStatement);
        }
    }
    
    /*
     * 批量执行 
     */  
    public static int[] executeBatch(String sql, List<List<Object>> params) throws SQLException {  
   
        int[] result = new int[] {};
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {  
            conn = ConnectionPool.getConnection(dsName);
            conn.setAutoCommit(false);
            preparedStatement = conn.prepareStatement(sql);
            for (List<Object> objs : params) {
                setParams(preparedStatement, objs.toArray());
                preparedStatement.addBatch();
            }
            result = preparedStatement.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            try {  
                conn.rollback();
            } catch (SQLException e1) {  
                throw e;
            }
            throw e;
        }  finally {  
            ConnectionPool.close(conn, preparedStatement);
        }
        return result;
    }
    
    private static void setParams(PreparedStatement pst, Object... paramters) throws SQLException {
        if (paramters != null) {
            for (int i = 0; i < paramters.length; i++) {
                if (paramters[i] instanceof Date) {
                    Date date = (Date)paramters[i];
                    pst.setTimestamp(i + 1, new Timestamp(date.getTime()));
                    continue;
                }
                pst.setObject(i + 1, paramters[i]);
            }
        }
    }
    
    private static <T> List<T> convertToList(List<Map<String, Object>> query, Class<T> clz) {
        List<T> result = new ArrayList<T>();
        for (Map<String, Object> map : query) {
            result.add(convertToObj(map, clz));
        }
        return result;
    }
    
    private static <T> T convertToObj(Map<String, Object> map, Class<T> clz) {
        if (map == null) {
            return null;
        }
        String json = DataToolUtils.serialize(map);
        return DataToolUtils.deserialize(json, clz);
    }
}
