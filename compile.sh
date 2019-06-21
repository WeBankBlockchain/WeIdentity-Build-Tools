#!/bin/bash
source ./script/common.inc
source run.config

set -e

if [[ "$@" == "--offline" ]];then
    OFFLINE_MODE="1"
    echo "Compile in offline mode.."
fi

#SOURCE_CODE_DIR=$(pwd)
SOLC=$(which fisco-solc)
WEB3J="${SOURCE_CODE_DIR}/script/web3sdk.sh"
chmod +x ${WEB3J}
CONFIG_FILE=${SOURCE_CODE_DIR}/conf/run.config
APP_XML_CONFIG_TPL=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml.tpl
APP_XML_CONFIG=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml
APP_XML_CONFIG_TMP=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml.tmp
FISCO_XML_CONFIG_TPL=${SOURCE_CODE_DIR}/script/tpl/fisco.properties.tpl
FISCO_XML_CONFIG=${SOURCE_CODE_DIR}/script/tpl/fisco.properties
FISCO_XML_CONFIG_TMP=${SOURCE_CODE_DIR}/script/tpl/fisco.properties.tmp

function compile_contract() 
{ 
    echo "Begin to check if contracts update or not."
    # check if contracts need to be compiled
    num=$(ls contracts|grep ".sol"|wc -l)
    if [ 0 -eq ${num} ];then
        echo "no contract updates, no need to compile contract."
        return
    else
        echo "contracts update, need to compile the contract."
    fi
 
    cd contracts/
    #begin to compile contracts
    #java package path for contract code
    package="com.webank.weid.contract"
    output_dir="${SOURCE_CODE_DIR}/output"
    echo "output_dir is $output_dir"
    local files=$(ls ./*.sol)
    #compile contract with web3j
    for itemfile in ${files}
    do
        local item=$(basename ${itemfile} ".sol")
        ${SOLC} --abi --bin --overwrite -o ${output_dir} ${itemfile}
        echo "${output_dir}/${item}.bin, ${output_dir}, ${package} "
        ${WEB3J} solidity generate  "${output_dir}/${item}.bin" "${output_dir}/${item}.abi" -o ${output_dir} -p ${package} 
    done
    
    cd ${SOURCE_CODE_DIR}/script
    if [ -d src/ ];then
        rm -rf src
    fi
    mkdir src
    cp -r ${output_dir}/com src/
    if [[ ${OFFLINE_MODE} == "1" ]];then
        gradle build --offline
    else
        gradle build
    fi
    cd ${SOURCE_CODE_DIR}
    build_classpath
    echo "Compile contracts done."
}

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
      if [ ! -z ${content} ];then
           content="${content}\n"
      fi
    	content="${content}<value>$var</value>"
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
    MYVARS='${BLOCKCHIAN_NODE_INFO}:${WEID_ADDRESS}:${CPT_ADDRESS}:${ISSUER_ADDRESS}:${EVIDENCE_ADDRESS}:${SPECIFICISSUER_ADDRESS}'
    if [ -f ${APP_XML_CONFIG} ];then
        rm ${APP_XML_CONFIG}
    fi
    envsubst ${MYVARS} < ${APP_XML_CONFIG_TPL} >${APP_XML_CONFIG}
    if [ -f ${APP_XML_CONFIG_TMP} ];then
        rm ${APP_XML_CONFIG_TMP}
    fi
    envsubst '${BLOCKCHIAN_NODE_INFO}' < ${APP_XML_CONFIG_TPL} >${APP_XML_CONFIG_TMP}
    cp ${APP_XML_CONFIG} ${SOURCE_CODE_DIR}/resources
    if [ -f ${FISCO_XML_CONFIG} ];then
        rm ${FISCO_XML_CONFIG}
    fi
    envsubst ${MYVARS} < ${FISCO_XML_CONFIG_TPL} >${FISCO_XML_CONFIG}
    if [ -f ${FISCO_XML_CONFIG_TMP} ];then
        rm ${FISCO_XML_CONFIG_TMP}
    fi
    envsubst '${BLOCKCHIAN_NODE_INFO}' < ${FISCO_XML_CONFIG_TPL} >${FISCO_XML_CONFIG_TMP}
    cp ${FISCO_XML_CONFIG} ${SOURCE_CODE_DIR}/resources
    cp ${SOURCE_CODE_DIR}/script/tpl/log4j2.xml ${SOURCE_CODE_DIR}/resources
    #cp ${SOURCE_CODE_DIR}/script/tpl/weidentity.properties ${SOURCE_CODE_DIR}/resources
    cp -rf ${SOURCE_CODE_DIR}/resources ${SOURCE_CODE_DIR}/src/main/
    
    if [ -d ${SOURCE_CODE_DIR}/dist/app ];then
        rm -rf ${SOURCE_CODE_DIR}/dist/app
    fi
    if [[ ${OFFLINE_MODE} == "1" ]];then
        gradle clean build --offline
    else
        gradle clean build
    fi
    build_classpath
    #compile_contract
    echo "compile finished."
}

function setup()
{
	if [ "${blockchain_fiscobcos_version}" = "1" ] || [ "${blockchain_fiscobcos_version}" = "2" ];then 
        cp ${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml.tpl-${blockchain_fiscobcos_version}.x ${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml.tpl
        cp ${SOURCE_CODE_DIR}/script/tpl/fisco.properties.tpl-${blockchain_fiscobcos_version}.x ${SOURCE_CODE_DIR}/script/tpl/fisco.properties.tpl
        cp ${SOURCE_CODE_DIR}/script/tpl/build.gradle-${blockchain_fiscobcos_version}.x ${SOURCE_CODE_DIR}/build.gradle
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
