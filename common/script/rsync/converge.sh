#!/bin/bash

cd ..
source ./script/common.inc

cd ${SOURCE_CODE_DIR}

build_classpath

echo "begin to converge data, please wait..."
java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.DataConverge

if [ ! $? -eq 0 ]; then
    echo "converge data faild, please check the log -> ../logs/error.log."
    exit $?;
fi
echo "converge data successfully."