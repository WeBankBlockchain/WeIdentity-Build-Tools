#!/bin/bash

cd ..
source ./script/common.inc

cd ${SOURCE_CODE_DIR}

if [ $# -lt 4 ] ;then
	echo "input error. please check your input."
    exit 1
fi

weid=$2
cpt_dir=$4

if [ "$5" = "--private-key" ];then
    private_key=$6
else
    we_address=`echo $weid|awk -F":" '{print $3}' `    
    private_key=${SOURCE_CODE_DIR}/output/create_weid/${we_address}/ecdsa_key
fi

build_classpath

if [ -f regist_cpt.out ];then
	rm -f  regist_cpt.out
fi

echo "begin to regist cpt, please wait..."
java -cp "$CLASSPATH" com.webank.weid.command.RegistCpt ${weid} ${cpt_dir} ${private_key}

if [ ! $? -eq 0 ]; then
    echo "regist cpt faild, please check."
    exit $?
fi

if [ ! -d ${SOURCE_CODE_DIR}/output/regist_cpt ];then
	mkdir -p  ${SOURCE_CODE_DIR}/output/regist_cpt
fi

if [ -f regist_cpt.out ];then
	mv regist_cpt.out ${SOURCE_CODE_DIR}/output/regist_cpt/
fi

echo "regist cpt finished."
