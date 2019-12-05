#!/bin/bash

if [ $# -lt 2 ];then
	echo "input error. please input like this: ./cpt_to_pojo.sh --cpt-list 1000,10001"
    exit 1
fi

cd ..
source run.config
source ./script/common.inc

cd ${SOURCE_CODE_DIR}

set -e

function generate_cpt_file()
{  
 	#unzip jsonschema2pojo tool
    schema2pojo_tool_dir=${SOURCE_CODE_DIR}/jsonschema2pojo-1.0.0
    if [ ! -d ${schema2pojo_tool_dir} ];then 
        cp ${SOURCE_CODE_DIR}/script/jsonschema2pojo-1.0.0.zip ./
        unzip -o jsonschema2pojo-1.0.0.zip >/dev/null
        	
    fi
    
    build_classpath

    java ${JAVA_OPTS} -cp "$CLASSPATH" com.webank.weid.command.CptToPojo $@
    
    if [ ! $? -eq 0 ]; then
	    echo "get cpt faild, please check the log -> ../logs/error.log."
	    exit $?;
	fi
}


function cpt_to_pojo()
{
	
	echo "Begin to package cpt to weidentity-cpt.jar..."
	
	 # get cpt 
    cpt_dir=${SOURCE_CODE_DIR}/cpt_dir
    if [ ! -d ${cpt_dir} ];then
        mkdir ${cpt_dir}
    fi
    
	mv Cpt*.json ${cpt_dir}
    
    for cpt_file in ${cpt_dir}/*.json
        do
            ${SOURCE_CODE_DIR}/jsonschema2pojo-1.0.0/bin/jsonschema2pojo --source ${cpt_file} --annotation-style NONE  --package "com.webank.weid.cpt" --target ${SOURCE_CODE_DIR}/cpt_dir/src/main/java
        done
    build_cpt_jar
}

function policy_to_pojo()
{
	
	echo "Begin to package cpt to weidentity-cpt.jar..."
	
	# get cpt 
    cpt_policy_dir=${SOURCE_CODE_DIR}/cpt_dir/policy
    if [ ! -d ${cpt_policy_dir} ];then
        mkdir -p ${cpt_policy_dir}
    fi
    
	mv Cpt*.json ${cpt_policy_dir}
    
    for cpt_file in ${cpt_policy_dir}/*.json
        do
            ${SOURCE_CODE_DIR}/jsonschema2pojo-1.0.0/bin/jsonschema2pojo --source ${cpt_file} --annotation-style NONE  --package "com.webank.weid.cpt.policy" --target ${SOURCE_CODE_DIR}/cpt_dir/src/main/java
        done
    
    build_cpt_jar
}

function build_cpt_jar()
{
	
    cp ${SOURCE_CODE_DIR}/script/cpt.build.gradle ${SOURCE_CODE_DIR}/cpt_dir/build.gradle
    cd ${SOURCE_CODE_DIR}/cpt_dir

    gradle build >/dev/null

	if [ ! $? -eq 0 ]; then
        echo "Package cpt faild, please check the log -> ${SOURCE_CODE_DIR}/logs/error.log."
    	exit $?;
	fi
	
	echo "The weidentity-cpt.jar can be found in ${SOURCE_CODE_DIR}/dist/app/"
	
    cd ${SOURCE_CODE_DIR}
}

function generate_presentation_policy()
{
	echo "Begin to generate presentation policy ..."
	
	build_classpath
	
	java -cp "$CLASSPATH" com.webank.weid.command.GeneratePolicy $@ --org-id ${org_id}
	
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
    
    generate_cpt_file $@
    
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
