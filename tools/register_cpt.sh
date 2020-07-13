#!/bin/bash

cd ..
source ./common/script/common.inc
source ./run.config
cd ${SOURCE_CODE_DIR}

if [ $# -lt 4 ] ;then
	echo "input error. please check your input."
    exit 1
fi

build_classpath

if [ -f regist_cpt.out ];then
	rm -f  regist_cpt.out
fi

echo "begin to regist cpt, please wait..."

if [ "$1" = "--private-key" ] || [ "$3" = "--private-key" ] || [ "$5" = "--private-key" ];then
    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.RegistCpt $@
else
	if [ "$1" = "--weid" ] ;then
		weid=$2
	elif [ "$3" = "--weid" ] ;then
		weid=$4
	elif [ "$5" = "--weid" ] ;then
		weid=$6
	else
		echo "Input error, a WeID is needed."
		exit 1
	fi
    we_address=`echo $weid|awk -F":" '{print $4}' `    
    private_key=${SOURCE_CODE_DIR}/output/create_weid/{cns_contract_follow}/${we_address}/ecdsa_key
    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.RegistCpt $@ --private-key ${private_key}
fi

if [ ! $? -eq 0 ]; then
    echo "Regist cpt faild, please check your input and see log : ${SOURCE_CODE_DIR}/logs/all.log."
    exit $?
fi

#if [ ! -d ${SOURCE_CODE_DIR}/output/regist_cpt ];then
#	mkdir -p  ${SOURCE_CODE_DIR}/output/regist_cpt
#fi

#if [ -f regist_cpt.out ];then
#	mv regist_cpt.out ${SOURCE_CODE_DIR}/output/regist_cpt/
#fi

echo "Execute succeed."
