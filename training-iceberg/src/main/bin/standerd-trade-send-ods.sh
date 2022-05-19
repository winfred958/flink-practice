#!/bin/bash

QUEUE_NAME=STDQueue

HOME_PATH=$(
  cd "$(dirname "$0")"
  cd ..
  pwd
)

source ~/.bash_profile

CMD=$(
  cat <<EOF
${FLINK_HOME}/bin/flink run \
  --class com.winfred.iceberg.trade.StandardTradeStream \
  --yarnship ${HOME_PATH}/lib/iceberg-flink-runtime-1.14-0.13.1.jar \
  --classpath file://${HOME_PATH}/lib/iceberg-flink-runtime-1.14-0.13.1.jar \
  --jobmanager yarn-cluster \
  --yarnjobManagerMemory 1024 \
  --yarnslots 1 \
  --yarntaskManagerMemory 2048 \
  --parallelism 5 \
  --detached \
  --yarnname NoteSendStreamOdsTable \
  --yarnqueue ${QUEUE_NAME} \
  ${HOME_PATH}/lib/training-iceberg.jar \
    --checkpoint-dir hdfs://spacex-hadoop-qa/flink/checkpoiont \
    --warehouse-path hdfs://spacex-hadoop-qa/iceberg/warehouse \
    --topic-names standard_trade_state \
    --namespace-name ods         \
    --table-name standard_trade

EOF
)

echo -e "CMD:\n${CMD}\n"

${CMD}
