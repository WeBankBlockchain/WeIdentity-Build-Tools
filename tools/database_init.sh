#!/bin/bash

cd ..
source ./common/script/common.inc

cd ${SOURCE_CODE_DIR}


build_classpath

echo "begin init database, please wait..."

java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.DataBaseInit $@

if [ ! $? -eq 0 ]; then
    echo "init faild, please check the log -> ../logs/error.log."
    exit $?;
fi