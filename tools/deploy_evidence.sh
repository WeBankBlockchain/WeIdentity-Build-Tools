#!/bin/bash

cd ..
source ./common/script/common.inc

cd ${SOURCE_CODE_DIR}

if [ $# -lt 1 ] ;then
    echo "input error. please check your input."
    exit 1
fi

build_classpath

echo "begin deploy evidence address into cns, please wait..."

java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.DeployEvidence $@

if [ ! $? -eq 0 ]; then
    echo "deploy faild, please check the log -> ../logs/error.log."
    exit $?;
fi