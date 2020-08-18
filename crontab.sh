#!/bin/bash

# base path
ROOT_DIR=$(pwd)
WEIDENTITY_CRON_JOB_TPL=$ROOT_DIR/common/script/tpl/weidentity-cron-job.tpl
WEIDENTITY_CRON_JOB=$ROOT_DIR/common/script/tpl/weidentity-cron-job

# default configuration
export CRONTAB_TIME="0 1 * * *"
export BUILD_TOOL_DIR="$ROOT_DIR"
export USER="root"

if [ ! -d "logs" ]; then 
    mkdir logs
fi

# make the file
FISCOVAS='${CRONTAB_TIME}:${BUILD_TOOL_DIR}:${USER}'
envsubst ${FISCOVAS}} < ${WEIDENTITY_CRON_JOB_TPL} >${WEIDENTITY_CRON_JOB}

# copy to /etc/cron.d/
cp $WEIDENTITY_CRON_JOB /etc/cron.d/

# remove a file
rm $WEIDENTITY_CRON_JOB