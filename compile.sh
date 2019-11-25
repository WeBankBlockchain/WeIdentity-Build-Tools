#!/bin/bash
source ./script/common.inc
source run.config

set -e

if [[ "$@" == "--offline" ]];then
    OFFLINE_MODE="1"
    echo "Compile in offline mode.."
fi

if [ ! -d dist/ ];then
    mkdir dist
fi
if [ ! -d dist/lib ];then
    mkdir dist/lib
fi
if [ ! -d dist/lib/NotoSansCJKtc-Regulat.ttf ];then
    cd dist/lib
    touch NotoSansCJKtc-Regular.ttf
    cd ../..
fi

#SOURCE_CODE_DIR=$(pwd)
CONFIG_FILE=${SOURCE_CODE_DIR}/conf/run.config
FISCO_XML_CONFIG_TPL=${SOURCE_CODE_DIR}/script/tpl/fisco.properties.tpl
FISCO_XML_CONFIG=${SOURCE_CODE_DIR}/script/tpl/fisco.properties
FISCO_XML_CONFIG_TMP=${SOURCE_CODE_DIR}/script/tpl/fisco.properties.tmp
WEIDENTITY_CONFIG_TPL=${SOURCE_CODE_DIR}/script/tpl/weidentity.properties.tpl
WEIDENTITY_CONFIG=${SOURCE_CODE_DIR}/script/tpl/weidentity.properties


function clean_config()
{
    cd ${SOURCE_CODE_DIR}
    echo "begin to clean config..."
    if [ -d bin/ ];then
        rm -rf bin/
    fi
    if [ -d ${SOURCE_CODE_DIR}/script/src/ ];then
        rm -rf ${SOURCE_CODE_DIR}/script/src/
    fi
    
    if [ -d build/ ];then
        rm -rf build/
    fi
    echo "clean finished..."
}

function compile()
{
    echo "begin to compile build tools..."
    cd ${SOURCE_CODE_DIR}
    #if more than one blockchain node exist, use this to seperate them
    OLD_IFS="$IFS"
    IFS=","
    array=($blockchain_address)
    IFS="$OLD_IFS"
    #fill with ip and port of blockchain nodes
    declare -i num
    num=1
    for var in ${array[@]}
    do
    declare -i length
    length=${#array[@]}
    if [ "${blockchain_fiscobcos_version}" = "1" ];then
      if [ $num -lt $length ];then
        content="${content}WeIdentity@$var,"
      fi
      if [ $num -eq $length ];then
        content="${content}WeIdentity@$var"
      fi
    elif [ "${blockchain_fiscobcos_version}" = "2" ];then
    	if [ $num -lt $length ];then
        content="${content}$var,"
      fi
      if [ $num -eq $length ];then
        content="${content}$var"
      fi
    else
    	echo "currently FISCO BCOS ${blockchain_fiscobcos_version}.x is not supported."
    fi
    num=${num}+1
    done
    
    export BLOCKCHIAN_NODE_INFO=$(echo -e ${content})
    export WEID_ADDRESS="0x0"
    export CPT_ADDRESS="0x0"
    export ISSUER_ADDRESS="0x0"
    export EVIDENCE_ADDRESS="0x0"
    export SPECIFICISSUER_ADDRESS="0x0"
    export CHAIN_ID=${chain_id}
    export FISCO_BCOS_VERSION=${blockchain_fiscobcos_version}
    MYVARS='${BLOCKCHIAN_NODE_INFO}:${WEID_ADDRESS}:${CPT_ADDRESS}:${ISSUER_ADDRESS}:${EVIDENCE_ADDRESS}:${SPECIFICISSUER_ADDRESS}:${FISCO_BCOS_VERSION}'
    FISCOVAS='${CHAIN_ID}:${FISCO_BCOS_VERSION}'
    envsubst ${FISCOVAS}} < ${FISCO_XML_CONFIG_TPL} >${FISCO_XML_CONFIG}
    if [ -f ${FISCO_XML_CONFIG_TMP} ];then
        rm ${FISCO_XML_CONFIG_TMP}
    fi
    cp ${FISCO_XML_CONFIG} ${SOURCE_CODE_DIR}/resources
    cp ${SOURCE_CODE_DIR}/script/tpl/log4j2.xml ${SOURCE_CODE_DIR}/resources
    #cp ${SOURCE_CODE_DIR}/script/tpl/weidentity.properties ${SOURCE_CODE_DIR}/resources
    cp -rf ${SOURCE_CODE_DIR}/resources ${SOURCE_CODE_DIR}/src/main/
    
    #modify weidentity properties
    export ORG_ID=${org_id}
    export JDBC_URL=${jdbc_url}
    export JDBC_USERNAME=${jdbc_username}
    export JDBC_PASSWORD=${jdbc_password}
    VARS='${BLOCKCHIAN_NODE_INFO}:${ORG_ID}:${JDBC_URL}:${JDBC_USERNAME}:${JDBC_PASSWORD}'
    envsubst ${VARS} < ${WEIDENTITY_CONFIG_TPL} >${WEIDENTITY_CONFIG}
    cp  ${WEIDENTITY_CONFIG} ${SOURCE_CODE_DIR}/resources
    
    
    if [ -d ${SOURCE_CODE_DIR}/dist/app ];then
        rm -rf ${SOURCE_CODE_DIR}/dist/app
    fi
    if [[ ${OFFLINE_MODE} == "1" ]];then
        gradle clean build --offline
    else
        gradle clean build
    fi
    build_classpath
    echo "compile finished."
}

function setup()
{
	if [ "${blockchain_fiscobcos_version}" = "1" ] || [ "${blockchain_fiscobcos_version}" = "2" ];then 
        cp ${SOURCE_CODE_DIR}/script/tpl/deploy_code/DeployContract.java-${blockchain_fiscobcos_version}.x ${SOURCE_CODE_DIR}/src/main/java/com/webank/weid/command/DeployContract.java
        cp ${SOURCE_CODE_DIR}/script/tpl/deploy_code/DeploySystemCpt.java-${blockchain_fiscobcos_version}.x ${SOURCE_CODE_DIR}/src/main/java/com/webank/weid/command/DeploySystemCpt.java
    else
    	echo "currently FISCO BCOS ${blockchain_fiscobcos_version}.x is not supported."
    	exit 1
    fi
}

function check_parameter()
{
    if [ -z ${blockchain_address} ];then
        echo "blockchain address is empty, please check the config."
        exit 1
    fi
    if [ -z ${blockchain_fiscobcos_version} ];then
        echo "blockchain version config is illegal, please check the config."
        exit 1
    fi
    if [ -z ${org_id} ];then
        echo "org id is empty, please check the config."
        exit 1
    fi
    if [ -z ${chain_id} ];then
        echo "chain id is empty, please check the config."
        exit 1
    fi
}

function main()
{
    check_parameter
    setup
    check_jdk
    compile
    clean_config
}

main
