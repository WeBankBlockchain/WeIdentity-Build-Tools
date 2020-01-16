#!/bin/bash

set -e

#down the build-tool-code
git clone https://github.com/WeBankFinTech/weid-build-tools.git
cd weid-build-tools

#switch branch
git checkout develop

#set the sdk version

if [ -n "$1" ] ;then
    sed -i "/^weidSdkVersion/cweidSdkVersion=$1" gradle.properties
fi

#build
gradle clean build

chmod u+x start.sh