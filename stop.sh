#!/bin/bash
buildTool_pid=

getPid() {
   buildTool_pid=`ps aux|grep "BuildToolApplication" | grep -v grep|awk '{print $2}'|head -1` 
}

getPid;

if [ -n "$buildTool_pid" ];then
   kill -9 $buildTool_pid
   echo "the server stop success."
else 
   echo "the server already stop."
fi