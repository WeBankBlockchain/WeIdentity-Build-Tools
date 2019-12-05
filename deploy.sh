#!/bin/bash
source ./script/common.inc
source run.config
set -e

#SOURCE_CODE_DIR=$(pwd)
FISCO_XML_CONFIG=${SOURCE_CODE_DIR}/script/tpl/fisco.properties
FISCO_XML_CONFIG_TMP=${SOURCE_CODE_DIR}/script/tpl/fisco.properties.tmp
FISCO_XML_CONFIG_TPL=${SOURCE_CODE_DIR}/script/tpl/fisco.properties.tpl

function modify_config()
{
    cd ${SOURCE_CODE_DIR}
    echo "begin to modify sdk config..."
    if [ ! -f weIdContract.address ];then
    	echo "deploy contract failed."
    	exit 1
    fi
    echo "contract is deployed with success."
    echo "===========================================."
    weid_address=$(cat weIdContract.address)
    echo "weid contract address is ${weid_address}"
    cpt_address=$(cat cptController.address)
    echo "cpt contract address is ${cpt_address}"
    issuer_address=$(cat authorityIssuer.address)
    echo "authority issuer contract address is ${issuer_address}"
    evidence_address=$(cat evidenceController.address)
    echo "evidence contract address is ${evidence_address}"
    specificIssuer_address=$(cat specificIssuer.address)
    echo "specificIssuer contract address is ${specificIssuer_address}"
    echo "===========================================."
    echo ""
    
    export WEID_ADDRESS=${weid_address}
    export CPT_ADDRESS=${cpt_address}
    export ISSUER_ADDRESS=${issuer_address}
    export EVIDENCE_ADDRESS=${evidence_address}
    export SPECIFICISSUER_ADDRESS=${specificIssuer_address}
    #export ORG_ID=${org_id}
    export CHAIN_ID=${chain_id}
    export FISCO_BCOS_VERSION=${blockchain_fiscobcos_version}
    MYVARS='${WEID_ADDRESS}:${CPT_ADDRESS}:${ISSUER_ADDRESS}:${EVIDENCE_ADDRESS}:${SPECIFICISSUER_ADDRESS}:${CHAIN_ID}:${FISCO_BCOS_VERSION}'
    
    if [ -f ${FISCO_XML_CONFIG} ];then
        rm ${FISCO_XML_CONFIG}
    fi
    envsubst ${MYVARS} < ${FISCO_XML_CONFIG_TPL} >${FISCO_XML_CONFIG}
    #envsubst ${ORG_ID} < ${WEIDENTITY_CONFIG} >${WEIDENTITY_CONFIG_TMP}
    cp ${FISCO_XML_CONFIG} ${SOURCE_CODE_DIR}/resources
   # cp -f ${WEIDENTITY_CONFIG_TMP} ${SOURCE_CODE_DIR}/resources/${WEIDENTITY_CONFIG}
}

function deploy_contract()
{
	echo " "
    echo "begin to deploy contract, please wait....."
    cd ${SOURCE_CODE_DIR}
    #deploy contract to your blockchain nodes
    build_classpath
    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.DeployContract
    
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
    
}

function deploy_system_cpt()
{
    echo "begin to deploy system contract..."
    cd ${SOURCE_CODE_DIR}
    build_classpath
    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.DeploySystemCpt

    if [ ! $? -eq 0 ]; then
        echo "deploy system cpt failed, please check."
        exit $?;
    fi

    echo "deploy system cpt done."
}

function clean_data()
{
    #delete useless files
    cd ${SOURCE_CODE_DIR} 
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

function check_node_cert(){

	cd ${SOURCE_CODE_DIR}/resources 
	if [ "${blockchain_fiscobcos_version}" = "1" ];then
        if [ ! -f  ca.crt -o ! -f  client.keystore ];then
        echo "ERROR : fisco bcos version is 1.3, ca.crt and client.keystore are needed."
        exit 1
      fi
    elif [ "${blockchain_fiscobcos_version}" = "2" ];then
        if [ ! -f  ca.crt -o ! -f  node.crt -o ! -f  node.key ];then
        echo "ERROR : fisco bcos version is 2.0. ca.crt, node.crt and node.key are needed."
        exit 1
        fi
    else
        echo "the version : ${blockchain_fiscobcos_version} is not supported, we only support FISCO BCOS 1.3 and 2.0."
        exit 1
   fi
}

function main()
{
    check_jdk
    check_node_cert
    deploy_contract
    modify_config
    deploy_system_cpt
    clean_data
}

main
