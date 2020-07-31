#!/bin/bash
source ./common/script/common.inc
source run.config

set -e

if [[ "$@" == "--offline" ]];then
   OFFLINE_MODE="1"
   echo "Compile in offline mode.."
fi

if [[ "$@" == "cn" || "$@" == "en" ]];then
    sed -i "/^repoType/crepoType=$1" gradle.properties
fi

if [ ! -d dist/ ];then
    mkdir dist
fi
if [ ! -d dist/lib ];then
    mkdir dist/lib
fi

#SOURCE_CODE_DIR=$(pwd)
FISCO_XML_CONFIG_TPL=${SOURCE_CODE_DIR}/common/script/tpl/fisco.properties.tpl
FISCO_XML_CONFIG=${SOURCE_CODE_DIR}/common/script/tpl/fisco.properties
FISCO_XML_CONFIG_TMP=${SOURCE_CODE_DIR}/common/script/tpl/fisco.properties.tmp
WEIDENTITY_CONFIG_TPL=${SOURCE_CODE_DIR}/common/script/tpl/weidentity.properties.tpl
WEIDENTITY_CONFIG=${SOURCE_CODE_DIR}/common/script/tpl/weidentity.properties


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
    export CHAIN_ID=${chain_id}
    export GROUP_ID=${group_id}
    export CNS_PROFILE_ACTIVE=${cns_profile_active}
    export FISCO_BCOS_VERSION=${blockchain_fiscobcos_version}
    FISCOVAS='${GROUP_ID}:${CHAIN_ID}:${FISCO_BCOS_VERSION}:${CNS_PROFILE_ACTIVE}'
    envsubst ${FISCOVAS}} < ${FISCO_XML_CONFIG_TPL} >${FISCO_XML_CONFIG}
    if [ -f ${FISCO_XML_CONFIG_TMP} ];then
        rm ${FISCO_XML_CONFIG_TMP}
    fi
    #cp ${SOURCE_CODE_DIR}/script/tpl/log4j2.xml ${SOURCE_CODE_DIR}/resources
    #cp -rf ${SOURCE_CODE_DIR}/resources ${SOURCE_CODE_DIR}/src/main/
    
    #modify weidentity properties
    export ORG_ID=${org_id}
    export AMOP_ID=${amop_id}
    export MYSQL_ADDRESS=${mysql_address}
    export MYSQL_DATABASE=${mysql_database}
    export MYSQL_USERNAME=${mysql_username}
    export MYSQL_PASSWORD=${mysql_password}
    VARS='${BLOCKCHIAN_NODE_INFO}:${ORG_ID}:${AMOP_ID}:${MYSQL_ADDRESS}:${MYSQL_DATABASE}:${MYSQL_USERNAME}:${MYSQL_PASSWORD}'
    envsubst ${VARS} < ${WEIDENTITY_CONFIG_TPL} >${WEIDENTITY_CONFIG}

    if [ -f build.gradle ]; then
        chmod u+x gradlew
        if [ -d ${SOURCE_CODE_DIR}/dist/app ];then
            rm -rf ${SOURCE_CODE_DIR}/dist/app
        fi
        if [[ ${OFFLINE_MODE} == "1" ]];then
            ./gradlew clean build --offline
        else
            ./gradlew clean build
        fi
    fi
    
    cp ${WEIDENTITY_CONFIG} ${SOURCE_CODE_DIR}/resources
    cp ${FISCO_XML_CONFIG} ${SOURCE_CODE_DIR}/resources
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
    if [ -z ${amop_id} ];then
        echo "amop id is empty, please check the config."
        exit 1
    fi
    if [ -z ${chain_id} ];then
        echo "chain id is empty, please check the config."
        exit 1
    fi
    if [ -z ${group_id} ];then
        echo "group id is empty, please check the config."
        exit 1
    fi
    # if [ -z ${mysql_address} ];then
    #    echo "mysql_address is empty, please check the config."
    #    exit 1
    # fi
    # if [ -z ${mysql_database} ];then
    #    echo "mysql_database is empty, please check the config."
    #    exit 1
    # fi
    # if [ -z ${mysql_username} ];then
    #    echo "mysql_username is empty, please check the config."
    #    exit 1
    # fi
    # if [ -z ${mysql_password} ];then
    #    echo "mysql_password is empty, please check the config."
    #    exit 1
    # fi
    if [ -z ${cns_profile_active} ];then
       echo "cns_profile_active is empty, please check the config."
       exit 1
    fi
    if [ "${cns_profile_active}" != "prd" ] &&  [ "${cns_profile_active}" != "stg" ] && [ "${cns_profile_active}" != "dev" ];then
        echo "the value of cns_profile_active error, please input: prd, stg, dev"
        exit 1
    fi
}

function check_font()
{
  md5file=`cat ext/NotoSansCJKtc-Regular.md5`
  md5font=`md5sum ext/NotoSansCJKtc-Regular.ttf | cut -d ' ' -f1`
  if [ "$md5file" != "$md5font" ];then
    echo "font file is broken."
    exit 1
  fi
  echo "font check successs."
}

function main()
{
    # check_font
    check_parameter
    # setup
    check_jdk
    compile
    clean_config
}

main
