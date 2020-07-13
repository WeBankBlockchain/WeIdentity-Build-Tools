#!/bin/bash
source ./common/script/common.inc

set -e

applicationFile=${SOURCE_CODE_DIR}/dist/conf/application.properties

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
CLASSPATH=${CLASSPATH}:${SOURCE_CODE_DIR}/dist/conf/

getPid;

if [ -n "$buildTool_pid" ];then
   echo "-----------------------------------------------"
   echo "The weid-build-tools web server has already started. pid = "$buildTool_pid
   echo "-----------------------------------------------"
   exit 1
fi

#start the application
nohup java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.app.BuildToolApplication &> /dev/null&

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
    echo "-----------------------------------------------"
    echo "The weid-build-tools web server started successfully."
    echo "The weid-build-tools web server url : http://127.0.0.1:"${port}"/index.html"
    echo "-----------------------------------------------"
else 
    if [ $count == 30 ]; then
        echo "-----------------------------------------------"
        echo "The weid-build-tools web server started timeout, please check the log -> ./logs/error.log."
        echo "-----------------------------------------------"
        exit 1;
    fi
    echo "-----------------------------------------------"
    echo "The weid-build-tools web server started error, please check the log -> ./logs/error.log."
    echo "-----------------------------------------------"
fi
