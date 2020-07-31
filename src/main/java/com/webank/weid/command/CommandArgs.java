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

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import lombok.Getter;

/**
 * command line parameter parser.
 *
 * @author tonychen 2019/6/4
 */
@Getter
public class CommandArgs {


    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = {"--weid"}, description = "weidentity did")
    private String weid;

    @Parameter(names = "--cpt-dir", description = "the directory which the cpt json files will be found and be registered to blockchain.")
    private String cptDir;

    @Parameter(names = "--private-key", description = "private key file")
    private String privateKey;

    @Parameter(names = "--cpt-list", description = "cpt id list")
    private String cptIdList;

    @Parameter(names = "--cpt-id", description = "cpt id")
    private String cptId;

    @Parameter(names = "--cpt-file", description = "cpt file")
    private String cptFile;

    @Parameter(names = "--org-id", description = "org name")
    private String orgId;
    
    @Parameter(names = "--chain-id", description = "chain Id")
    private String chainId;

    @Parameter(names = "--type", description = "type")
    private String type;
    
    @Parameter(names = "--pojoId", description = "pojoId")
    private String pojoId;

    @Parameter(names = "--remove-issuer", description = "remove authority issuer")
    private String removedIssuer;

    @Parameter(names = "--policy-id", description = "policy id")
    private String policyId;
    
    @Parameter(names = "--policy-file", description = "policy file name")
    private String policyFileName;
    
    @Parameter(names = "--async-status", description = "async status")
    private String asyncStatus;

    @Parameter(names = "--data-time", description = "data time")
    private String dataTime;
    
    @Parameter(names = "--group-id", description = "group id")
    private String groupId;
    
    @Parameter(names = "--cns", description = "cns")
    private String cns;
}
