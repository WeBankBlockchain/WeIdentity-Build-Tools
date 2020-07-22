#!/bin/bash

cd ..
source ./common/script/common.inc

cd ${SOURCE_CODE_DIR}

build_classpath

java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.UpgradeDataBucket

if [ ! $? -eq 0 ]; then
    echo "update faild, please check the log -> ../logs/error.log."
    exit $?;
fi