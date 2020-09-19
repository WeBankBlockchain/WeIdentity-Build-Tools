#!/bin/bash

rootDir=../../../
installZipDir=weid-build-tools/common/script/install
project=weid-build-tools

set -- `getopt n: "$@"`
while [ -n "$1" ]
do
    case "$1" in 
     -n) 
        project=$2
        shift ;;
    esac
    shift 
done

# analysis dataBase config.
mysql_address=
mysql_database=
mysql_username=
mysql_password=
webase_port=5101
analysisDataBase() {
    mysql_address=$(grep "mysql_address" ${1} | cut -d'=' -f2 | sed 's/\r//')
    mysql_database=$(grep "mysql_database" ${1} | cut -d'=' -f2 | sed 's/\r//')
    mysql_username=$(grep "mysql_username" ${1} | cut -d'=' -f2 | sed 's/\r//')
    mysql_password=$(grep "mysql_password" ${1} | cut -d'=' -f2 | sed 's/\r//')
}

# install webase
function installWebase() {
    # check
    # 2. Analysis of database configuration
    dbConfig=${rootDir}run.config
    analysisDataBase ${dbConfig}
    if [ -z "${mysql_address}" ] || [ -z "${mysql_database}" ] || [ -z "${mysql_username}" ] || [ -z "${mysql_password}" ];then
        if [ -f "${rootDir}/output/.run.config" ];then
            dbConfig=${rootDir}/output/.run.config
            analysisDataBase ${dbConfig}
        fi
    fi
    if [ -z "${mysql_address}" ] || [ -z "${mysql_database}" ] || [ -z "${mysql_username}" ] || [ -z "${mysql_password}" ];then
        echo "Err: Please use weid-build-tools to configure the database."
        exit 1
    fi

    # install
    fiscoBcosBrowserDir=fisco-bcos-browser
    # 1. check the script dir.
    if [ ! -d "${fiscoBcosBrowserDir}/script/" ];then
        mkdir -p ${fiscoBcosBrowserDir}/script/
    fi

    cd ${fiscoBcosBrowserDir}
    webTargetDir=${rootDir}../dist/web/static/weid/weid-build-tools/webase-browser/

    # 2. check the install script.
    if [ ! -f "script/init.sh" ];then
        echo 'begin download init.sh'
        cd script
        wget -c https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/FISCO-BCOS/fisco-bcos-browser/releases/download/v2.2.1-Integrated/init.sh
        cd ..
    fi
    if [ ! -f "script/init.sh" ];then
        echo "Err: can not download the init.sh for fisco-bcos-browser."
        exit 1
    fi

    # 3. cp the init.sh
    cp ./script/init.sh ./
    chmod u+x init.sh
    
    # 4. installing
    ./init.sh -P ${webase_port} -l "jdbc:mysql://${mysql_address}/${mysql_database}?useUnicode=true&characterEncoding=utf8" -u ${mysql_username} -p ${mysql_password}
    if [ ! -d "./server/" ] || [ ! -d "./web/" ];then
        echo "Err: the fisco-bcos-browser install fail."
        exit 1
    fi
    chmod u+x ./server/*.sh

    # 5. copy the web/* to ${rootDir}/dist/web/static/weid/weid-build-tools/webase-browser/
    if [ ! -d "${webTargetDir}" ];then
        mkdir -p ${webTargetDir}
    fi
    cp -r web/* ${webTargetDir}

    # 6. rm init.sh and web
    rm -rf init.sh
    rm -rf web

    echo "--------------------------------------------------------------------------"
    echo "fisco-bcos-browser is installed successfully, please go to the fisco-bcos-browser/server directory and start the server."
    echo "Example: cd fisco-bcos-browser/server && ./start.sh"
    echo "--------------------------------------------------------------------------"
}

if [ ${project} == "webase" ];then
    installWebase
fi