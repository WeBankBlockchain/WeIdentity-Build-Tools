#!/bin/bash

function  install_font()
{
        sudo mkdir -p /usr/share/fonts/chinese&&
                sudo cp ./NotoSansCJKtc-Regular.ttf  /usr/share/fonts/chinese/&&
                sudo apt install xfonts-utils -y || sudo yum -y install fontconfig ttmkfdir mkfontscale&&
                sudo mkfontscale&&
                sudo mkfontdir&&
                sudo fc-cache -fv

        font_result=`fc-list`
        result=$(echo $font_result | grep "NotoSansCJKtc-Regular.ttf")

        if [[ "$result" != "" ]]
        then
                echo "font installed successfully."
        else
                echo "font installation  failed"
        fi
}

function main()
{
        install_font
}

main