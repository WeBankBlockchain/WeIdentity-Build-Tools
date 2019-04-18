#!/bin/bash
source ./common.inc

set -e

#SOURCE_CODE_DIR=$(pwd)
APP_XML_CONFIG=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml
APP_XML_CONFIG_TMP=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml.tmp

function modify_config()
{
    cd ${SOURCE_CODE_DIR}
    echo "begin to modify sdk config..."
    #modify applicationContext.xml with newly deployed contract address
    weid_address=$(cat weIdContract.address)
    cpt_address=$(cat cptController.address)
    issuer_address=$(cat authorityIssuer.address)
    evidence_address=$(cat evidenceController.address)
    export WEID_ADDRESS=${weid_address}
    export CPT_ADDRESS=${cpt_address}
    export ISSUER_ADDRESS=${issuer_address}
    export EVIDENCE_ADDRESS=${evidence_address}
    MYVARS='${WEID_ADDRESS}:${CPT_ADDRESS}:${ISSUER_ADDRESS}:${EVIDENCE_ADDRESS}'
    if [ -f ${APP_XML_CONFIG} ];then
        rm ${APP_XML_CONFIG}
    fi
    envsubst ${MYVARS} < ${APP_XML_CONFIG_TMP} >${APP_XML_CONFIG}
    cp ${APP_XML_CONFIG} ${SOURCE_CODE_DIR}/resources
    echo "modify sdk config finished..."
}

function deploy_contract()
{
    echo "begin to deploy contract..."
    cd ${SOURCE_CODE_DIR}
    #deploy contract to your blockchain nodes
    build_classpath
    java -cp "$CLASSPATH" com.webank.weid.command.DeployContract
    
    if [ ! $? -eq 0 ]; then
        echo "deploy contract failed, please check."
        exit $?;
    fi
    

    if [ ! -d ${SOURCE_CODE_DIR}/output/keyPair ];then
        
        mkdir -p ${SOURCE_CODE_DIR}/output/keyPair
    fi

    mv public_key ${SOURCE_CODE_DIR}/output/keyPair
    mv private_key ${SOURCE_CODE_DIR}/output/keyPair
    
    echo "contract deployment done."
}

function clean_data()
{
    #delete useless files
    if [ -f weIdContract.address ];then
        rm -f weIdContract.address
    fi
    if [ -f cptController.address ];then
        rm -f cptController.address
    fi
    if [ -f authorityIssuer.address ];then
        rm -f authorityIssuer.address
    fi
    if [ -f evidenceController.address ];then
        rm -f evidenceController.address
    fi
}

function main()
{
    check_jdk
    deploy_contract
    modify_config
    clean_data
}

main
