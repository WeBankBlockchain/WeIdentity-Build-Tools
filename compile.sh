#!/bin/bash
set -e

source_code_dir=$(pwd)
cd $source_code_dir

SOLC=$(which fisco-solc)
WEB3J="${source_code_dir}/script/web3sdk.sh"
chmod +x ${WEB3J}
config_file=${source_code_dir}/run.config
app_xml_config_tpl=${source_code_dir}/script/tpl/applicationContext.xml.tpl
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

function compile_contract() 
{ 
    echo "Begin to check if contracts update or not."
    num=$(ls contracts|grep ".sol"|wc -l)
    if [ 0 -eq ${num} ];then
        echo "no contract updates, no need to compile contract."
        return
    else
        echo "contracts update, need to compile the contract."
    fi
 
    cd contracts/
    
    package="com.webank.weid.contract"
    output_dir="${source_code_dir}/output"
    echo "output_dir is $output_dir"
    local files=$(ls ./*.sol)
    for itemfile in ${files}
    do
        local item=$(basename ${itemfile} ".sol")
        ${SOLC} --abi --bin --overwrite -o ${output_dir} ${itemfile}
        echo "${output_dir}/${item}.bin, ${output_dir}, ${package} "
        ${WEB3J} solidity generate  "${output_dir}/${item}.bin" "${output_dir}/${item}.abi" -o ${output_dir} -p ${package} 
    done
    
    cd ${source_code_dir}/script
    if [ -d src/ ];then
        rm -rf src
    fi
    mkdir src
    cp -r ${output_dir}/com src/
    gradle build
    cd ${source_code_dir}
    build_classpath
    echo "Compile contracts done."
}

function clean_config()
{
    cd ${source_code_dir}
    echo "begin to clean config..."
    if [ -d bin/ ];then
    	rm -rf bin/
    fi
    if [ -d ${source_code_dir}/script/src/ ];then
    	rm -rf ${source_code_dir}/script/src/
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
    cd ${source_code_dir}
    node_addr=$(grep "blockchain.node.address" $config_file |awk -F"=" '{print $2}')
    OLD_IFS="$IFS"
    IFS=","
    array=($node_addr)
    IFS="$OLD_IFS"
    content=
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
    if [ -f ${app_xml_config} ];then
        rm ${app_xml_config}
    fi
    envsubst ${MYVARS} < ${app_xml_config_tpl} >${app_xml_config}
    if [ -f ${app_xml_config_tmp} ];then
        rm ${app_xml_config_tmp}
    fi
    envsubst '${BLOCKCHIAN_NODE_INFO}' < ${app_xml_config_tpl} >${app_xml_config_tmp}
    cp ${app_xml_config} ${source_code_dir}/resources
    #cp ${app_xml_config} ${app_xml_config_tmp}
    cp ${source_code_dir}/script/tpl/log4j2.xml ${source_code_dir}/resources
    cp -rf ${source_code_dir}/resources ${source_code_dir}/src/main/
    gradle build
    build_classpath
    compile_contract
    echo "compile finished."
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

function main()
{
    #compile_contract
    check_jdk
    compile
    clean_config
}

main
