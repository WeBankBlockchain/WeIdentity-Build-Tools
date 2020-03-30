#!/bin/bash
source ./script/common.inc

set -e

applicationFile=${SOURCE_CODE_DIR}/src/main/resources/application.properties

buildTool_pid=
running=false
port=$(grep "server\.port" $applicationFile |awk -F "=" '{print $2}')

getPid() {
   buildTool_pid=`ps aux|grep "BuildToolApplication" | grep -v grep|awk '{print $2}'|head -1` 
}
value=
checkServer() {
    getPid
    if [ -n "$buildTool_pid" ] ;then
        value=$(lsof -i:$port|grep ${buildTool_pid}|awk '{print $2}')
        if [ "${value}" == "${buildTool_pid}" ] ;then
            running=true
        fi;
    fi
}

#begin build classpath
CLASSPATH=${SOURCE_CODE_DIR}/resources:${SOURCE_CODE_DIR}/dist/app/*:${SOURCE_CODE_DIR}/dist/lib/*

#set the application.properties in to classpath
CLASSPATH=${CLASSPATH}:${SOURCE_CODE_DIR}/src/main/resources/

getPid;

if [ -n "$buildTool_pid" ];then
   kill -9 $buildTool_pid
   buildTool_pid=
fi

#start the application
nohup java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.app.BuildToolApplication &> nohup.out &

echo -n "starting..."

count=0
while [ ${running} = false ] 
do
    checkServer
    sleep 1
    count=$(expr $count + 1)
    if [ $count == 30 ]; then
        echo "."
        break;
    fi
    echo -n "."
done

echo 

if [ ${running} = true ];then
    echo "the server start successfully."
    echo "the server url:  http://localhost:"${port}"/index.html"
else 
    if [ $count == 30 ]; then
        echo "the server start timeout, please restart the server."
        exit 1;
    fi
    echo "the server start error, please check the log."
fi
