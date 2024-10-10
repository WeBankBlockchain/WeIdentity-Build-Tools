#!/bin/bash

cd ..
source ./common/script/common.inc

cd ${SOURCE_CODE_DIR}

if [ $# -lt 1 ] ;then
    echo "input error. please check your input."
    exit 1
fi

build_classpath

echo "begin enable share into cns, please wait..."

java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.EnableShareCns $@

if [ ! $? -eq 0 ]; then
    echo "share faild, please check the log -> ../logs/error.log."
    exit $?;
fi