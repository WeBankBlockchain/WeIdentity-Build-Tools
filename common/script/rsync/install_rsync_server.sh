#!/bin/bash

currentPath=`pwd`

# check rsync
if rpm -q rsync &>/dev/null; then
    echo "rsync is already installed."
else
    echo "begin install rsync..."
    yum install rsync
fi

# chek dir
rsync_conf=$currentPath/rsync.conf
logFile=$(grep "log file=" $rsync_conf |awk -F"=" '{print $2}')
pidFile=$(grep "pid file=" $rsync_conf |awk -F"=" '{print $2}')

logDir=$(dirname $logFile)
pidDir=$(dirname $pidFile)

if [ ! -d $logDir ];then
    mkdir $logDir
fi

if [ ! -d $pidDir ];then
    mkdir $pidDir
fi

# check rsync server
rsync_pid_path=$pidFile
if [ -f $rsync_pid_path ];then
    rsync_pid=$(cat $rsync_pid_path)
    echo "the rsync server is starting, the pid = "$rsync_pid
    exit 1;
fi

echo "begin start rsync server..."
rsync --daemon --config=$rsync_conf

if [ -f $rsync_pid_path ];then
    rsync_pid=$(cat $rsync_pid_path)
    echo "the rsync start success. the pid = "$rsync_pid
else 
    echo "the rsync start fail."
fi

