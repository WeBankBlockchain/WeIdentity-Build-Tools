#!/bin/bash
buildTool_pid=

getPid() {
   buildTool_pid=`ps aux|grep "BuildToolApplication" | grep -v grep|awk '{print $2}'|head -1` 
}

getPid;

if [ -n "$buildTool_pid" ];then
   kill -9 $buildTool_pid
   echo "-----------------------------------------------"
   echo "The weid-build-tools web server stopped successfully."
   echo "-----------------------------------------------"
else 
   echo "-----------------------------------------------"
   echo "The weid-build-tools web server already stopped successfully."
   echo "-----------------------------------------------"
fi