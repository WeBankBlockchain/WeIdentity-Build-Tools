#!/bin/bash
set -e

source_code_dir=$(pwd)
cd $source_code_dir

app_xml_config=${source_code_dir}/script/tpl/applicationContext.xml
app_xml_config_tmp=${source_code_dir}/script/tpl/applicationContext.xml.tmp

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
        JAVACMD="java"
        which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

    Please set the JAVA_HOME variable in your environment to match the
    location of your Java installation."
    fi
    echo "jdk check finished."
}

function modify_config()
{
    cd ${source_code_dir}
    echo "begin to modify sdk config..."
    weid_address=$(cat weIdContract.address)
    cpt_address=$(cat cptController.address)
    issuer_address=$(cat authorityIssuer.address)
    export WEID_ADDRESS=${weid_address}
    export CPT_ADDRESS=${cpt_address}
    export ISSUER_ADDRESS=${issuer_address}
    MYVARS='${WEID_ADDRESS}:${CPT_ADDRESS}:${ISSUER_ADDRESS}'
    if [ -f ${app_xml_config} ];then
	rm ${app_xml_config}
    fi
    envsubst ${MYVARS} < ${app_xml_config_tmp} >${app_xml_config}
    cp ${app_xml_config} ${source_code_dir}/resources
    echo "modify sdk config finished..."
}

function deploy_contract()
{
    echo "begin to deploy contract..."
    cd ${source_code_dir}
    build_classpath
    java -cp "$CLASSPATH" com.webank.weid.contract.deploy.DeployContract
    echo "contract deployment done."
}

function build_classpath()
{

    CLASSPATH=${source_code_dir}/resources
    for jar_file in ${source_code_dir}/dist/app/*.jar
        do
            CLASSPATH=${CLASSPATH}:${jar_file}
        done

    for jar_file in ${source_code_dir}/dist/lib/*.jar
        do
            CLASSPATH=${CLASSPATH}:${jar_file}
        done

}

function clean_data()
{
	 if [ -f weIdContract.address ];then
        rm -f weIdContract.address
    fi
    if [ -f cptController.address ];then
        rm -f cptController.address
    fi
    if [ -f authorityIssuer.address ];then
        rm -f authorityIssuer.address
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
