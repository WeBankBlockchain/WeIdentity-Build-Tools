#!/bin/bash

set -e

cd ..
source ./common/script/common.inc

cd ${SOURCE_CODE_DIR}

build_classpath

echo "begin execute batch transaction, please wait..."
if [ ! -n "$1" ]; then
    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.BatchTransaction
else 
    if [ ! -n "$2" ]; then
        java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.BatchTransaction --data-time $1
    else 
        java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.BatchTransaction --data-time $1 --async-status $2
    fi;

fi;

if [[ $? -ne 0 ]]; then
    echo "send batch transaction fail, please check the log -> ../logs/error.log."
    exit $?;
fi
echo "execute batch transaction successfully."