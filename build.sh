#!/bin/bash

#set the sdk version

if [ -n "$1" ] ;then
    sed -i "/^weidSdkVersion/cweidSdkVersion=$1" gradle.properties
fi

#build
gradle clean build

chmod u+x start.sh
chmod u+x stop.sh