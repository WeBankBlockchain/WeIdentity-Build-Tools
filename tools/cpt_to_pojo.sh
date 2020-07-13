#!/bin/bash

if [ $# -lt 2 ];then
	echo "input error. please input like this: ./cpt_to_pojo.sh --cpt-list 1000,10001"
    exit 1
fi

cd ..
source run.config
source ./common/script/common.inc

cd ${SOURCE_CODE_DIR}

set -e

function cpt_to_pojo()
{
	
	echo "Begin to package cpt to weidentity-cpt.jar..."
	
	build_classpath

    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.ToPojo $@

    if [ ! $? -eq 0 ]; then
        echo "cptToPojo faild, please check the log -> ../logs/error.log."
        exit $?;
    fi
}

function policy_to_pojo()
{
	
	echo "Begin to package policy to weidentity-cpt.jar..."
    
    build_classpath
    
    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.ToPojo $@
    
    if [ ! $? -eq 0 ]; then
        echo "policyToPojo faild, please check the log -> ../logs/error.log."
        exit $?;
    fi
}


function generate_presentation_policy()
{
	echo "Begin to generate presentation policy ..."
	if [ ! -f "pojoId" ];then
	   echo "can not generate presentation policy, please check the log -> ../logs/error.log."
	   exit 1;
	fi
	
	pojoId=$(cat pojoId)
	rm pojoId
	build_classpath
	
	java -cp "$CLASSPATH" com.webank.weid.command.GeneratePolicy $@ --pojoId ${pojoId}
	if [ ! $? -eq 0 ]; then
        echo "generate presentation policy faild, please check the log -> ../logs/error.log."
        exit $?;
    fi
    presentation_policy=${SOURCE_CODE_DIR}/output/presentation_policy
    if [ ! -d ${presentation_policy} ];then
            mkdir ${presentation_policy}
    fi
    if [ -f "presentation_policy.json" ];then
            mv presentation_policy.json ${presentation_policy}
    else
            echo "generate presentation policy failed."
            exit 1
    fi
    echo "Presentation Policy template is successfully generated, you can find it at ${SOURCE_CODE_DIR}/output/presentation_policy."
}


function main()
{
    check_jdk
    if [ "$1" = "--cpt-list" ];then
    	cpt_to_pojo $@
        generate_presentation_policy $@
   
    elif [ "$1" = "--policy-file" ]; then
    	policy_to_pojo $@
    else
    	echo "input error!"
    	exit 1
    fi
	
}

main $@
