#/bin/sh

cd ..
source ./common.inc

cd ${SOURCE_CODE_DIR}

build_classpath

echo "begin to register specific issuers and types, please wait..."
java -cp "$CLASSPATH" com.webank.weid.command.RegistSpecificIssuer ${SOURCE_CODE_DIR}/conf/regist_specific_issuer_config/parameter.conf ${SOURCE_CODE_DIR}/output/keyPair/ecdsa_key

if [ ! $? -eq 0 ]; then
    echo "register specific issuers and types failed, please check error logs for details."
    exit $?
fi

echo "specific issuers and types have been successfully registered on blockchain."
