#/bin/bash

if [ $# -lt 4 ] ;then
	echo "input error. please input like this: ./cpt_to_pojo.sh --cpt-list 1000,10001"
    exit 1
fi

var weid
if [ "$1" = "--weid" ] ;then
	weid=$2
fi

if [ "$3" = "--private-key" ] ;then
	private_key=$4
fi


cd ..
source ./script/common.inc

cd ${SOURCE_CODE_DIR}

build_classpath

echo "begin to regist authority issuer, please wait..."
java -cp "$CLASSPATH" com.webank.weid.command.RegistAuthorityIssuer ${weid} ${org_name} ${private_key}

if [ ! $? -eq 0 ]; then
    echo "regist authority issuer faild, please check."
    exit $?
fi

echo "authority issuer has been successfully registed on blockchain."
