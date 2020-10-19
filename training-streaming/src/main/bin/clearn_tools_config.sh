#!/bin/bash

/usr/local/services/cloud_clear_disk-1.0/conf/conf.d/flink.conf

CONTEXT=$(
  cat <<EOF
delete 3 /data/emr/flink/logs/ flink.log.*
delete 3 /data/emr/flink/logs/splitfile *
EOF
)

test.conf <"${CONTEXT}"
