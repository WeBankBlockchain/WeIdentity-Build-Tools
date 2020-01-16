#!/bin/bash

cd ..
source ./script/common.inc

cd ${SOURCE_CODE_DIR}

build_classpath

echo "Creating WeID, please wait..."
java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.CreateWeId

if [ ! $? -eq 0 ]; then
    echo "Create WeID faild, please check the log -> ../logs/error.log."
    exit $?;
fi

if [ -f "weid" ];then
    rm weid
fi
