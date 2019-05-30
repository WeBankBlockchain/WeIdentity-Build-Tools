#!/bin/bash

cd ..
source ./common.inc

cd ${SOURCE_CODE_DIR}

set -e

#SOURCE_CODE_DIR=$(pwd)


function cpt_to_pojo()
{
    echo "begin to generate pojo from cpt..."
    schema2pojo_tool_dir=${SOURCE_CODE_DIR}/jsonschema2pojo-1.0.0
    if [ ! -d ${schema2pojo_tool_dir} ];then 
        wget https://github.com/joelittlejohn/jsonschema2pojo/releases/download/jsonschema2pojo-1.0.0/jsonschema2pojo-1.0.0.zip
        unzip -o jsonschema2pojo-1.0.0.zip
    fi
 
    # 1.get cpt 
    cpt_dir=${SOURCE_CODE_DIR}/cpt_dir
    if [ ! -d ${cpt_dir} ];then
        mkdir ${cpt_dir}
    fi

    build_classpath

    java -cp "$CLASSPATH" com.webank.weid.command.CptToPojo ${SOURCE_CODE_DIR}/conf/cpt_to_pojo_config/parameter.conf 
    mv Cpt*.json ${cpt_dir}
    
    for cpt_file in ${cpt_dir}/*.json
        do
            ${SOURCE_CODE_DIR}/jsonschema2pojo-1.0.0/bin/jsonschema2pojo --source $cpt_file --annotation-style NONE  --package "com.webank.weid.cpt" --target ${SOURCE_CODE_DIR}/cpt_dir/src/main/java
        done

    cp ${SOURCE_CODE_DIR}/script/cpt.build.gradle ${SOURCE_CODE_DIR}/cpt_dir/build.gradle
    cd ${cpt_dir}

    gradle build

    cd ${SOURCE_CODE_DIR}
    
}

function generate_presentation_policy()
{
	echo "begin to generate presentation policy ..."
	
	build_classpath
	
	java -cp "$CLASSPATH" com.webank.weid.command.GeneratePolicy ${SOURCE_CODE_DIR}/conf/cpt_to_pojo_config/parameter.conf
	
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
    echo "presentation policy template is successfully generated."
    
}

function main()
{
    check_jdk
    
    cpt_to_pojo
    
	generate_presentation_policy
}

main
