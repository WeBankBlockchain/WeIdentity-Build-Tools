#/bin/sh

source ./common.inc

build_classpath

echo "begin to regist authority issuer, please wait..."
java -cp "$CLASSPATH" com.webank.weid.command.RegistAuthorityIssuer ${SOURCE_CODE_DIR}/conf/registAuthorityIssuer_config/parameter.conf ${SOURCE_CODE_DIR}/output/keyPair/private_key

if [ ! $? -eq 0 ]; then
    echo "regist authority issuer faild, please check."
    exit $?
fi

echo "authority issuer has been successfully registed on blockchain."
