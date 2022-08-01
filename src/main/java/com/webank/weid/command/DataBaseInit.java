

package com.webank.weid.command;

import lombok.extern.slf4j.Slf4j;

import com.webank.weid.config.StaticConfig;
import com.webank.weid.service.DataBaseService;

/**
 * @author tonychen 2019/4/11
 */
@Slf4j
public class DataBaseInit extends StaticConfig {

    private static DataBaseService dataBaseService = new DataBaseService();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {
        log.info("[DataBaseInit] begin init DataBase.");
        dataBaseService.initDataBase();
        System.exit(0);
    }
}

