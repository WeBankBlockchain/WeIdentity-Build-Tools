#!/bin/bash
set -e

SOURCE_CODE_DIR=$(pwd)
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
    else
        JAVACMD="java"
        which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

    Please set the JAVA_HOME variable in your environment to match the
    location of your Java installation."
    fi
    echo "jdk check finished."
}

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
    java -cp "$CLASSPATH" com.webank.weid.contract.deploy.DeployContract
    echo "contract deployment done."
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
