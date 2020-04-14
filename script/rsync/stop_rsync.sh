#!/bin/bash

# check rsync server
rsync_pid_path=./run/rsync.pid
if [ -f $rsync_pid_path ];then
    rsync_pid=$(cat $rsync_pid_path)
    if [ ! -z $rsync_pid ];then
        kill -9 $rsync_pid
	rm $rsync_pid_path
        echo "the rsync server stop success."
        exit 1;
    fi
fi

echo "the rsync server does not exist."
