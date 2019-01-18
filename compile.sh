#!/bin/bash
source ./run.config

set -e

SOURCE_CODE_DIR=$(pwd)
SOLC=$(which fisco-solc)
WEB3J="${SOURCE_CODE_DIR}/script/web3sdk.sh"
chmod +x ${WEB3J}
CONFIG_FILE=${SOURCE_CODE_DIR}/run.config
APP_XML_CONFIG_TPL=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml.tpl
APP_XML_CONFIG=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml
APP_XML_CONFIG_TMP=${SOURCE_CODE_DIR}/script/tpl/applicationContext.xml.tmp

function check_jdk()
{
    echo "Begin to check jdk..."
    # Determine the Java command to use to start the JVM.
    if [ -n "$JAVA_HOME" ] ; then
        if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
            # IBM's JDK on AIX uses strange locations for the executables
            JAVACMD="$JAVA_HOME/jre/sh/java"
        else
            JAVACMD="$JAVA_HOME/bin/java"
    fi
        if [ ! -x "$JAVACMD" ] ; then
            echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME
                Please set the JAVA_HOME variable in your environment to match the
                location of your Java installation."
        fi
    else
        which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

    Please set the JAVA_HOME variable in your environment to match the
    location of your Java installation."
    fi
    echo "jdk check finished."
}

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
    gradle build
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
    if [ -d output/ ];then
        rm -rf output/
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
    for var in ${array[@]}
    do
    if [ ! -z ${content} ];then
         content="${content}\n"
    fi
        content="${content}<value>WeIdentity@$var</value>"
    done
    export BLOCKCHIAN_NODE_INFO=$(echo -e ${content})
    export WEID_ADDRESS="0x0"
    export CPT_ADDRESS="0x0"
    export ISSUER_ADDRESS="0x0"
    MYVARS='${BLOCKCHIAN_NODE_INFO}:${WEID_ADDRESS}:${CPT_ADDRESS}:${ISSUER_ADDRESS}'
    if [ -f ${APP_XML_CONFIG} ];then
        rm ${APP_XML_CONFIG}
    fi
    envsubst ${MYVARS} < ${APP_XML_CONFIG_TPL} >${APP_XML_CONFIG}
    if [ -f ${APP_XML_CONFIG_TMP} ];then
        rm ${APP_XML_CONFIG_TMP}
    fi
    envsubst '${BLOCKCHIAN_NODE_INFO}' < ${APP_XML_CONFIG_TPL} >${APP_XML_CONFIG_TMP}
    cp ${APP_XML_CONFIG} ${SOURCE_CODE_DIR}/resources
    cp ${SOURCE_CODE_DIR}/script/tpl/log4j2.xml ${SOURCE_CODE_DIR}/resources
    cp -rf ${SOURCE_CODE_DIR}/resources ${SOURCE_CODE_DIR}/src/main/
    gradle build
    build_classpath
    compile_contract
    echo "compile finished."
}

function build_classpath()
{

    CLASSPATH=${SOURCE_CODE_DIR}/resources
    for jar_file in ${SOURCE_CODE_DIR}/dist/app/*.jar
        do
            CLASSPATH=${CLASSPATH}:${jar_file}
        done

    for jar_file in ${SOURCE_CODE_DIR}/dist/lib/*.jar
        do
            CLASSPATH=${CLASSPATH}:${jar_file}
        done
}

function main()
{
    check_jdk
    compile
    clean_config
}

main
