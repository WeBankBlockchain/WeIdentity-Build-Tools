#!/bin/bash

if [ $# -lt 2 ];then
	echo "input error."
    exit 1
fi

cd ..
source run.config
source ./common/script/common.inc

cd ${SOURCE_CODE_DIR}

set -e

function check_command()
{
    echo "begin to execute checking command..."

    build_classpath

    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.app.AppCommand $@
    
    if [ ! $? -eq 0 ]; then
        echo "check faild, please see the log -> ${SOURCE_CODE_DIR}/logs/error.log."
        exit $?;
    fi
	
    echo "check with success."
	
    cd ${SOURCE_CODE_DIR}
    
}

function main()
{
    check_jdk
    
    check_command $@
    
}

main $@
