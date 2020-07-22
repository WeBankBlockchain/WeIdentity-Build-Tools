#!/bin/bash

source ./run.config
TOP_PATH=$(pwd)
WEID_PREFIX="did:weid:"
CREATE_WEID=
WEID_ADDRESS=

function create_weid(){
    cd ./tools
    echo "begin to create weid..."
    if [ -d $TOP_PATH/output/create_weid/ ];then
        rm -rf $TOP_PATH/output/create_weid/
    fi
    
    chmod +x *.sh
    ./create_weid.sh no_del
    
    if [ $? -eq 0 ] ;then
        CREATE_WEID=$(cat ../weid)
        WEID_ADDRESS=`echo $CREATE_WEID | cut -d : -f 4`
        echo "create_weid success-$WEID_ADDRESS"
        rm ../weid
        cd $TOP_PATH/tools
    else
        echo "create_weid failed."
        exit $?
    fi
    echo "create weid finished..."
}

function regist_authority_issuer(){
    echo "begin to regist authority issuer..."
    ./register_authority_issuer.sh --weid $CREATE_WEID --org-id test
    if [ $? -ne 0 ];then
        echo "regist authority issuer failed."
        exit $?
    fi
    echo "regist authority issuer finished."
    
    echo "begin to remove authority issuer..."
    ./register_authority_issuer.sh --remove-issuer $CREATE_WEID 
    if [ $? -ne 0 ];then
        echo "remove authority issuer failed."
        exit $?
    fi
    echo "remove authority issuer finished."
    
    echo "begin to regist authority issuer..."
    ./register_authority_issuer.sh --weid $CREATE_WEID --org-id test
    if [ $? -ne 0 ];then
        echo "regist authority issuer failed."
        exit $?
    fi
    echo "regist authority issuer finished."
}

function regist_cpt(){
    echo "begin to regist designated cpt, cptId is '9999'..."
    ./register_cpt.sh --weid $CREATE_WEID --cpt-dir test_data/single/ --cpt-id 9999
    if [ $? -eq 0 ] && [ -e "$TOP_PATH/output/regist_cpt/regist_cpt.out" ];then
        echo "regist designated cptId of '9999' success"
    else
        echo "regist designated cptId of '9999' failed"
        exit $?
    fi
    echo "regist designated cpt finished."
    
    echo "begin to regist single cpt..."
    ./register_cpt.sh --weid $CREATE_WEID --cpt-dir test_data/single/
    if [ $? -eq 0 ] && [ -e "$TOP_PATH/output/regist_cpt/regist_cpt.out" ];then
        echo "regist single cpt success"
    else
        echo "regist single cpt failed"
        exit $?
    fi
    echo "regist single cpt finished."
    
    echo "begin to regist single cpt with privateKey..."    
    ./register_cpt.sh --weid $CREATE_WEID --cpt-dir test_data/single/ --private-key $TOP_PATH/output/create_weid/{cns_contract_follow}/$WEID_ADDRESS/ecdsa_key
    if [ $? -eq 0 ];then
        echo "regist single cpt with privateKey success"
    else
        echo "regist single cpt with privateKey failed"
        exit $?
    fi
    echo "regist single cpt with privateKey finished."
    
    echo "begin to regist batch cpt..."
    ./register_cpt.sh --weid $CREATE_WEID --cpt-dir test_data/batch/
    if [ $? -eq 0 ] && [ -e "$TOP_PATH/output/regist_cpt/regist_cpt.out" ];then
        echo "regist batch cpt success"
    else
        echo "regist batch cpt failed"
        exit $?
    fi
    echo "regist batch cpt finished."  
}


function cpt_to_pojo(){
    echo "begin to cpt to pojo..."
    ./cpt_to_pojo.sh --cpt-list 1000,1001
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
    regist_cpt
    cpt_to_pojo
    regist_specific_issuer
}

main
