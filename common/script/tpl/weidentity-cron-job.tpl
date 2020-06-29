# This is an asynchronous uplink job
${CRONTAB_TIME} ${USER} . /etc/profile;cd ${BUILD_TOOL_DIR}/tools/ && bash batch_transaction.sh > ${BUILD_TOOL_DIR}/logs/batch_transaction_$(date "+\%Y\%m\%d\%H\%M\%S").log 2>&1
