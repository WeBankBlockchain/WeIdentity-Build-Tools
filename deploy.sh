#!/bin/bash
source ./script/common.inc
source run.config
echo "org_id:${org_id}"
set -e

#SOURCE_CODE_DIR=$(pwd)
APP_XML_CONFIG=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml
APP_XML_CONFIG_TMP=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml.tmp
FISCO_XML_CONFIG=${SOURCE_CODE_DIR}/script/tpl/fisco.properties
FISCO_XML_CONFIG_TMP=${SOURCE_CODE_DIR}/script/tpl/fisco.properties.tmp
WEIDENTITY_CONFIG_TPL=${SOURCE_CODE_DIR}/script/tpl/weidentity.properties.tpl
WEIDENTITY_CONFIG=${SOURCE_CODE_DIR}/script/tpl/weidentity.properties

function modify_config()
{
    cd ${SOURCE_CODE_DIR}
    echo "begin to modify sdk config..."
    #modify applicationContext.xml with newly deployed contract address
    weid_address=$(cat weIdContract.address)
    cpt_address=$(cat cptController.address)
    issuer_address=$(cat authorityIssuer.address)
    evidence_address=$(cat evidenceController.address)
    specificIssuer_address=$(cat specificIssuer.address)
    export WEID_ADDRESS=${weid_address}
    export CPT_ADDRESS=${cpt_address}
    export ISSUER_ADDRESS=${issuer_address}
    export EVIDENCE_ADDRESS=${evidence_address}
    export SPECIFICISSUER_ADDRESS=${specificIssuer_address}
    #export ORG_ID=${org_id}
    export CHAIN_ID=${chain_id}
    MYVARS='${WEID_ADDRESS}:${CPT_ADDRESS}:${ISSUER_ADDRESS}:${EVIDENCE_ADDRESS}:${SPECIFICISSUER_ADDRESS}:${CHAIN_ID}'
    if [ -f ${APP_XML_CONFIG} ];then
        rm ${APP_XML_CONFIG}
    fi
    
    if [ "${blockchain_fiscobcos_version}" = "2" ];then
    	envsubst ${MYVARS} < ${APP_XML_CONFIG_TMP} >${APP_XML_CONFIG}
    	cp ${APP_XML_CONFIG} ${SOURCE_CODE_DIR}/resources
    fi
    
    if [ -f ${FISCO_XML_CONFIG} ];then
        rm ${FISCO_XML_CONFIG}
    fi
    envsubst ${MYVARS} < ${FISCO_XML_CONFIG_TMP} >${FISCO_XML_CONFIG}
    #envsubst ${ORG_ID} < ${WEIDENTITY_CONFIG} >${WEIDENTITY_CONFIG_TMP}
    cp ${FISCO_XML_CONFIG} ${SOURCE_CODE_DIR}/resources
   # cp -f ${WEIDENTITY_CONFIG_TMP} ${SOURCE_CODE_DIR}/resources/${WEIDENTITY_CONFIG}
    echo "modify sdk config finished..."
}

function deploy_contract()
{
    export ORG_ID=${org_id}
    envsubst '${ORG_ID}' < ${WEIDENTITY_CONFIG_TPL} >${WEIDENTITY_CONFIG}
    cp  ${WEIDENTITY_CONFIG} ${SOURCE_CODE_DIR}/resources

    echo "begin to deploy contract..."
    cd ${SOURCE_CODE_DIR}
    #deploy contract to your blockchain nodes
    build_classpath
    java -cp "$CLASSPATH" com.webank.weid.command.DeployContract
    
    if [ ! $? -eq 0 ]; then
        echo "deploy contract failed, please check."
        exit $?;
    fi
    

    if [ -d ${SOURCE_CODE_DIR}/output/admin ];then
        
        rm -rf ${SOURCE_CODE_DIR}/output/admin
    fi
    mkdir -p ${SOURCE_CODE_DIR}/output/admin

    mv ecdsa_key.pub ${SOURCE_CODE_DIR}/output/admin
    mv ecdsa_key ${SOURCE_CODE_DIR}/output/admin
    
    echo "contract deployment done."
}

function deploy_system_cpt()
{
    echo "begin to deploy system contract..."
    cd ${SOURCE_CODE_DIR}
    build_classpath
    java -cp "$CLASSPATH" com.webank.weid.command.DeploySystemCpt

    if [ ! $? -eq 0 ]; then
        echo "deploy system cpt failed, please check."
        exit $?;
    fi

    echo "deploy system cpt done."
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
    if [ -f specificIssuer.address ];then
        rm -f specificIssuer.address
    fi
}

function main()
{
    check_jdk
    deploy_contract
    modify_config
    deploy_system_cpt
    clean_data
}

main
