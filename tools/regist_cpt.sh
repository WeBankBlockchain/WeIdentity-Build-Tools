#!/bin/sh

cd ..
source ./common.inc

cd ${SOURCE_CODE_DIR}

if [ $# -lt 1 ] ;then
	echo "input error. please input your private key file path."
fi

build_classpath

if [ -f regist_cpt.out ];then
	rm -f  regist_cpt.out
fi

echo "begin to regist cpt, please wait..."
java -cp "$CLASSPATH" com.webank.weid.command.RegistCpt ${SOURCE_CODE_DIR}/conf/regist_cpt_conf/parameter.conf ${SOURCE_CODE_DIR}/conf/regist_cpt_conf $1

if [ ! $? -eq 0 ]; then
    echo "regist cpt faild, please check."
    exit $?
fi

if [ -f regist_cpt.out ];then
	mv regist_cpt.out ${SOURCE_CODE_DIR}/output/regist_cpt
fi

echo "regist cpt finished."
