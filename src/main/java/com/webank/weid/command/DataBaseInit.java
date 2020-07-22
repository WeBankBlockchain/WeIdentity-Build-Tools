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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.config.StaticConfig;
import com.webank.weid.service.DataBaseService;

/**
 * @author tonychen 2019/4/11
 */
public class DataBaseInit extends StaticConfig {

    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(DataBaseInit.class);

    private static DataBaseService dataBaseService = new DataBaseService();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {
        logger.info("[DataBaseInit] begin init DataBase.");
        dataBaseService.initDataBase();
        System.exit(0);
    }
}

