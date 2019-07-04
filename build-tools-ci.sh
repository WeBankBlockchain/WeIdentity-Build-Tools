#!/bin/bash

TOP_PATH=$(pwd)
WEID_PREFIX="did:weid:1:"
CREATE_WEID=

function create_weid(){
    cd ./tools
    echo "begin to create weid..."
    if [ -d $TOP_PATH/output/create_weid/ ];then
        rm -rf $TOP_PATH/output/create_weid/
    fi
    
    chmod +x *.sh
    ./create_weid.sh
    
    if [ $? -eq 0 ] && [ -d "$TOP_PATH/output/create_weid/" ];then
        cd $TOP_PATH/output/create_weid/
        CREATE_WEID=$WEID_PREFIX$(ls -l | awk '/^d/{print $NF}')
        echo "create_weid : $CREATE_WEID"
        cd $TOP_PATH/tools
    else
        echo "create_weid failed."
        exit $?
    fi
    echo "create weid finished..."
}

function regist_authority_issuer(){
    echo "begin to regist authority issuer..."
    ./regist_authority_issuer.sh --weid $CREATE_WEID --org-id test
    if [ $? -ne 0 ];then
        echo "regist authority issuer failed."
        exit $?
    fi
    echo "regist authority issuer finished."
}

function regist_designated_cpt(){
    echo "begin to regist designated cpt, cptId is '9999'..."
    ./regist_cpt.sh --weid $CREATE_WEID --cpt-dir $TOP_PATH/claim/ --cpt-id 9999
    if [ $? -eq 0 ] && [ -e "$TOP_PATH/output/regist_cpt/regist_cpt.out" ];then
        echo "regist designated cptId of '9999' success"
    else
        echo "regist designated cptId of '9999' failed"
        exit $?
    fi
    echo "regist designated cpt finished."
}

function regist_cpt(){
    echo "begin to regist cpt..."
    ./regist_cpt.sh --weid $CREATE_WEID --cpt-dir $TOP_PATH/claim/
    if [ $? -eq 0 ] && [ -e "$TOP_PATH/output/regist_cpt/regist_cpt.out" ];then
        echo "regist cpt success"
    else
        echo "regist cpt failed"
        exit $?
    fi
    echo "regist cpt finished."
}

function cpt_to_pojo(){
    echo "begin to cpt to pojo..."
    ./cpt_to_pojo.sh --cpt-list 1000
    if [ $? -eq 0 ] && [ -e "$TOP_PATH/output/presentation_policy/presentation_policy.json" ];then
        echo "cpt to pojo success"
    else
        echo "cpt to pojo failed"
        exit $?
    fi
    echo "cpt to pojo finished."
}

function regist_specific_issuer(){
    echo "begin to regist specific issuer..."
    ./register_specific_issuer.sh --weid $CREATE_WEID --type college
    if [ $? -ne 0 ];then
        echo "regist specific issuer failed."
        exit $?
    fi
    echo "regist specific issuer finished."
}


function main(){
    create_weid
    regist_authority_issuer
    regist_designated_cpt
    regist_cpt
    cpt_to_pojo
    regist_specific_issuer
}

main
