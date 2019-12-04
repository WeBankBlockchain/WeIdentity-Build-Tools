#/bin/bash

cd ..
source run.config
source ./script/common.inc

if [ $# -lt 2 ] ;then
	echo "input error."
    exit 1
fi

if [ "$1" = "--weid" ] ;then
	weid=$2
fi


cd ${SOURCE_CODE_DIR}

build_classpath

echo "please wait..."
private_key=${SOURCE_CODE_DIR}/output/admin/ecdsa_key
java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.RegistAuthorityIssuer $@ --private-key ${private_key}

if [ ! $? -eq 0 ]; then
    echo "ERROR, please check your input and see log ${SOURCE_CODE_DIR}/logs/all.log."
    exit $?
fi

echo "Execute succeed."
