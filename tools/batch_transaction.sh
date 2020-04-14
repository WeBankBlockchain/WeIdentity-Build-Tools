#!/bin/bash

cd ..
source ./script/common.inc

cd ${SOURCE_CODE_DIR}

build_classpath

echo "begin to send batch transaction, please wait..."
java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.BatchTransaction

if [ ! $? -eq 0 ]; then
    echo "send batch transaction fail, please check the log -> ../logs/error.log."
    exit $?;
fi
echo "send batch transaction successfully."