#!/bin/sh

source ./common.inc

build_classpath

echo "begin to regist cpt, please wait..."
java -cp "$CLASSPATH" com.webank.weid.command.RegistCpt ${SOURCE_CODE_DIR}/conf/registCpt_conf/parameter.conf ${SOURCE_CODE_DIR}/conf/registCpt_conf ${SOURCE_CODE_DIR}/conf/private_key

if [ ! $? -eq 0 ]; then
    echo "regist cpt faild, please check."
    exit $?
fi

echo "regist cpt finished."
