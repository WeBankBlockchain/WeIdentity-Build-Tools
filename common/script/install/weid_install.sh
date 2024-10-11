#!/bin/bash

repo=cn
version=latest.release
installZipDir=weid-build-tools/common/script/install
project=weid-build-tools
port=default

set -- `getopt v:t:n:p: "$@"`
while [ -n "$1" ]
do
    case "$1" in 
     -v) 
        version=$2 
        shift ;;
     -t) 
        repo=$2
        shift ;;
     -n) 
        project=$2
        shift ;;
     -p) 
        port=$2
        expr ${port} "+" 10 &> /dev/null
        if [ ! $? -eq 0 ];then
           echo "Err: input port: $port invalid."
           exit 1
        fi
        shift ;;
    esac
    shift 
done

# analysis dataBase config.
mysql_address=
mysql_database=
mysql_username=
mysql_password=
analysisDataBase() {
    mysql_address=$(grep "mysql_address" ${1} | cut -d'=' -f2 | sed 's/\r//')
    mysql_database=$(grep "mysql_database" ${1} | cut -d'=' -f2 | sed 's/\r//')
    mysql_username=$(grep "mysql_username" ${1} | cut -d'=' -f2 | sed 's/\r//')
    mysql_password=$(grep "mysql_password" ${1} | cut -d'=' -f2 | sed 's/\r//')
}

# install webase
function installWebase() {

    if [ ${port} == "default" ];then
        port=5101
    else
        sed -i "/^proxy\.target\.url/cproxy\.target\.url=127.0.0.1:$port" weid-build-tools/dist/conf/application.properties 
    fi

    # check
    # 1. check the weid-build-tools/run.config
    if [ ! -f "./weid-build-tools/run.config" ]; then
        echo "Err: Please install weid-build-tools before installing webase."
        exit 1
    fi

    # 2. Analysis of database configuration
    dbConfig=./weid-build-tools/run.config
    analysisDataBase ${dbConfig}
    if [ -z "${mysql_address}" ] || [ -z "${mysql_database}" ] || [ -z "${mysql_username}" ] || [ -z "${mysql_password}" ];then
        if [ -f "./weid-build-tools/output/.run.config" ];then
            dbConfig=./weid-build-tools/output/.run.config
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
    webTargetDir=../weid-build-tools/dist/web/static/weid/weid-build-tools/webase-browser/

    # 2. check the install script.
    if [ ! -f "script/init.sh" ];then
        echo 'begin download init.sh'
        cd script
        wget -c https://github.com/FISCO-BCOS/fisco-bcos-browser/releases/download/v2.2.1-Integrated/init.sh
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
    ./init.sh -P ${port} -l "jdbc:mysql://${mysql_address}/${mysql_database}?useUnicode=true&characterEncoding=utf8" -u ${mysql_username} -p ${mysql_password}
    if [ ! -d "./server/" ] || [ ! -d "./web/" ];then
        echo "Err: the fisco-bcos-browser install fail."
        exit 1
    fi
    chmod u+x ./server/*.sh

    # 5. copy the web/* to ../weid-build-tools/dist/web/static/weid/weid-build-tools/webase-browser/
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
    if [ ! ${port} -eq 5101 ];then
        echo "note: please restart the weid-build-tools server."
    fi
    echo "--------------------------------------------------------------------------"
}

# install weid-build-tools
function installBuildTools() {

    if [ ${port} == "default" ];then
        port=6021
    fi

    if [ ! -d ${installZipDir} ];then
        mkdir -p ${installZipDir}
    fi

    # 1. down weid-install-tools.zip
    if [ ! -d "weid-install-tools" ];then
        # check the install zip is exists in ${installZipDir}
        if [ -f "${installZipDir}/weid-install-tools.zip" ]; then
            cp ${installZipDir}/weid-install-tools.zip ./
        fi
        if [ ! -f "weid-install-tools.zip" ]; then
            echo "begin download the weid-install-tools.zip..."
            wget -c https://github.com/WeBankBlockchain/WeIdentity-Build-Tools/releases/download/v1.0.12/weid-install-tools.zip
        fi
        # unzip weid-install-tools.zip
        unzip weid-install-tools.zip
        # delete weid-install-tools.zip
        if [ ! -f "${installZipDir}/weid-install-tools.zip" ]; then
            cp weid-install-tools.zip ${installZipDir}/
        fi
        rm -rf weid-install-tools.zip
        if [ ! -d "weid-install-tools" ];then
            echo "unzip weid-install-tools.zip fail."
            exit 1
        fi
    fi

    cd weid-install-tools

    # 2. process args
    echo "the current version is $version"
    sed -i "/^repo/crepo=$repo" gradle.properties
    sed -i "/^buildToolVersion/cbuildToolVersion=$version" gradle.properties

    # 3. grant to gradlew and build
    chmod u+x gradlew
    ./gradlew clean build -Dorg.gradle.internal.http.socketTimeout=6000000 -Dorg.gradle.internal.http.connectionTimeout=6000000

    if [ -d "dist/lib" ];then
        echo "begin complete the package..."
        build_jar=$(`echo ls dist/lib/weid-build-tools*.jar`)
        build_jar=${build_jar##*/}
        if [ ! -f dist/lib/$build_jar ];then
            echo "Err: can not found the weid-build-tools.jar."
            exit 1
        fi

        rm -rf dist/app/
        mkdir -p dist/app/ && mv dist/lib/$build_jar dist/app/
        cd dist/app && jar -xvf $build_jar >/dev/null
        if [ ! -d file/ ];then
            echo "Err: jar -xvf $build_jar has error."
            exit 1
        fi

        echo "the current weid-build-tools:$build_jar"
        mv $build_jar file/dist/app/
        mv ../../dist/lib/* file/dist/lib/
        cd ../../../
        echo "dependency package preparation complete."
    else
        echo "Err: complete fail."
        exit 1
    fi

    # 4.mk the root dir and delete the weid-install-tools dir
    if [ ! -d weid-build-tools/ ];then
        mkdir weid-build-tools
    fi
    rm -rf weid-build-tools/dist
    cp -r weid-install-tools/dist/app/file/* ./weid-build-tools/
    rm -rf weid-install-tools

    # 5.grant and check
    cd weid-build-tools
    chmod u+x *.sh
    if [ ! $? -eq 0 ]; then
        echo "Err: the weid-build-tools install fail."
        exit $?
    fi
    
    # 6.modify port
    if [ ! ${port} -eq 6021 ];then
        sed -i "/^server\.port/cserver\.port=$port" dist/conf/application.properties
    fi
    echo "--------------------------------------------------------------------------"
    echo "weid-build-tools is installed successfully, please go to the weid-build-tools directory and start the server."
    echo "Example: cd weid-build-tools && ./start.sh"
    echo "--------------------------------------------------------------------------"
}

if [ ${project} == "webase" ];then
    installWebase
else
    installBuildTools
fi