#!/bin/sh

source ./common.inc

build_classpath

echo "begin to create weidentity did, please wait..."
java -cp "$CLASSPATH" com.webank.weid.command.CreateWeId

if [ ! $? -eq 0 ]; then
    echo "Create weid faild, please check it."
    exit $?;
fi

 if [ ! -d ${SOURCE_CODE_DIR}/output/createWeId ];then
        
        mkdir -p ${SOURCE_CODE_DIR}/output/createWeId
fi

if [ -f "weId" ];then
    weid=$(cat weId)
    OLD_IFS="$IFS"
    IFS=":"
    array=($weid)
    IFS="$OLD_IFS"
    weid_address=${array[2]}
    echo "weid_address=${weid_address}"
    mkdir -p ${SOURCE_CODE_DIR}/output/createWeId/${weid_address}
    mv public_key ${SOURCE_CODE_DIR}/output/createWeId/${weid_address}/
    mv private_key ${SOURCE_CODE_DIR}/output/createWeId/${weid_address}/
    mv weId ${SOURCE_CODE_DIR}/output/createWeId/${weid_address}/
fi

echo "new weidentity did has been created."
