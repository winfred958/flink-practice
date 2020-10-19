#!/bin/bash
HOME_PATH=$(
  cd "$(dirname "$0")"
  cd ..
  pwd
)

START_TIME=$(date +%s)

TMP_DIR_PATH=${HOME_PATH}/tmp

mkdir "${TMP_DIR_PATH}"
cd "${TMP_DIR_PATH}"

git clone https://github.com/apache/flink.git
git checkout release-1.6.4
git pull
git log -3

CMD=$(
  cat <<EOF
mvn clean install --projects flink-connectors --also-make --also-make-dependents -Pinclude-kinesis -DskipTests
EOF
)
echo "${CMD}"
${CMD}

# 清理
rm -rf "${TMP_DIR_PATH}"

END_TIME=$(date +%s)

echo -e "耗时: $(expr ${END_TIME} - ${START_TIME})s"
echo -e "耗时: $((${END_TIME} - ${START_TIME}))s"
