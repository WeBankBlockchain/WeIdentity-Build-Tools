#!/bin/bash

repo=cn
version=latest.integration

set -- `getopt v:t: "$@"`
while [ -n "$1" ]
do
    case "$1" in 
     -v) 
     	 version=$2 
         shift ;;
     -t) 
		 repo=$2
         shift ;;
    esac
    shift 
done

# 1. down weid-install-tools.zip
if [ ! -d "weid-install-tools" ];then
	if [ ! -f "weid-install-tools.zip" ]; then
		echo "begin download the weid-install-tools.zip..."
		if [[ ${repo} == "cn" ]];then
		  wget -c https://www.fisco.com.cn/cdn/weevent/weidentity/download/releases/weid-install-tools.zip
		else 
		  wget -c https://github.com/WeBankFinTech/weid-build-tools/raw/master/common/script/install/weid-install-tools.zip
		fi
	fi
	# unzip weid-install-tools.zip
	unzip weid-install-tools.zip
	# delete weid-install-tools.zip 
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
./gradlew clean build

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
echo "the weid-build-tools install successfully, please go to weid-build-tools and start server. example: cd weid-build-tools && ./start.sh"