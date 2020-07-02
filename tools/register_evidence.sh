#!/bin/bash

cd ..
source ./common/script/common.inc

cd ${SOURCE_CODE_DIR}

if [ $# -lt 4 ] ;then
    echo "input error. please check your input."
    exit 1
fi

build_classpath

echo "begin register evidence address into cns, please wait..."

java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.RegisterEvidenceByGroup $@

if [ ! $? -eq 0 ]; then
    echo "register faild, please check the log -> ../logs/error.log."
    exit $?;
fi