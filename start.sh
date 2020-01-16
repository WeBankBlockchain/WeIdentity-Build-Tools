#!/bin/bash
source ./script/common.inc

set -e

#begin build classpaht
build_classpath

#set the application.properties in to classpath
CLASSPATH=${CLASSPATH}:${SOURCE_CODE_DIR}/src/main/resources/

#start the application
nohup java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.app.BuildToolApplication &> nohup.out &