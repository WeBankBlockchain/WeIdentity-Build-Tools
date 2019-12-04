#/bin/sh

cd ..
source run.config
source ./script/common.inc

if [ $# -lt 2 ] ;then
	echo "input error."
    exit 1
fi

if [ "$1" = "--weid" ] ;then
	weid=$2
elif [ "$3" = "--weid" ] ;then
  weid=$4
fi

if [ "$1" = "--type" ] ;then
	type=$2
elif [ "$3" = "--type" ] ;then
  type=$4
fi


cd ${SOURCE_CODE_DIR}

build_classpath

echo "Begin to register specific issuers and types, please wait..."

private_key=${SOURCE_CODE_DIR}/output/admin/ecdsa_key
java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.RegisterSpecificIssuer $@ --private-key ${private_key}

if [ ! $? -eq 0 ]; then
    echo "Register specific issuers and types failed, please check error logs for details."
    exit $?
fi

echo "Specific issuers and types have been successfully registered on blockchain."
