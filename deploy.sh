#!/bin/bash
source ./common/script/common.inc
source run.config
set -e

#SOURCE_CODE_DIR=$(pwd)
applicationFile=${SOURCE_CODE_DIR}/dist/conf/application.properties
port=$(grep "server\.port" $applicationFile |awk -F "=" '{print $2}')

function reloadAddressForWeb() {
    export WEB_PID=`ps aux|grep "BuildToolApplication" | grep -v grep|awk '{print $2}'|head -1`
    if [ -n "$WEB_PID" ];then
        curl http://localhost:${port}/reloadAddress
    fi
}

function show_address()
{
    cd ${SOURCE_CODE_DIR}
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
}

function deploy_contract()
{
	echo " "
    echo "begin to deploy contract, please wait....."
    cd ${SOURCE_CODE_DIR}
    #deploy contract to your blockchain nodes
    build_classpath

    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.DeployContract --chain-id ${chain_id}  $@ 
    
    if [ ! $? -eq 0 ]; then
        echo "deploy contract failed, please check."
        exit $?;
    fi
    

    #if [ -d ${SOURCE_CODE_DIR}/output/admin ];then
    #    rm -rf ${SOURCE_CODE_DIR}/output/admin
    #fi
    #mkdir -p ${SOURCE_CODE_DIR}/output/admin
    
    #the copy action in deploy contract
    #mv ecdsa_key.pub ${SOURCE_CODE_DIR}/output/admin
    #mv ecdsa_key ${SOURCE_CODE_DIR}/output/admin
    
    rm -f ecdsa_key
    rm -f ecdsa_key.pub
    rm -f hash
    rm -f weid
    
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
    deploy_contract $@
    show_address
    #modify_config
    # deploy systemCpt in deploy contract
    #deploy_system_cpt
    clean_data
    reloadAddressForWeb
}

main $@
