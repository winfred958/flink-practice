#!/bin/bash
HOME_PATH=$(
  cd "$(dirname "$0")"
  cd ..
  pwd
)

#ENV_PATH=${HOME_PATH}/config/env.sh
#
#echo -e "====================== ENV_PATH = ${ENV_PATH}"
#
#source ${ENV_PATH} ${HOME_PATH}

TAG=${1}

if [[ "x${TAG}" == "x" ]]; then
  echo -e "需要按tag发布, 请确认tag编号"
  exit -1
else
  echo -e "tag=${TAG}\n"
fi

START_TIME=$(date +%s)

TMP_DIR_PATH=${HOME_PATH}/tmp
TARGET_DIR_PATH=${HOME_PATH}/lib

mkdir ${TMP_DIR_PATH}
cd ${TMP_DIR_PATH}

# clone
git clone https://github.com/winfred958/flink-practice.git
GIT_WORK_DIR=${TMP_DIR_PATH}/flink-practice
cd ${GIT_WORK_DIR}

git checkout ${TAG}

git pull

git log -3

# 开始编译打包
MODEL_NAME=training-streaming

CMD=$(
  cat <<EOF
mvn clean package --projects ${MODEL_NAME} --also-make --also-make-dependents
EOF
)

echo ${CMD}
${CMD}

# cp
cp ${GIT_WORK_DIR}/${MODEL_NAME}/target/${MODEL_NAME}.jar ${TARGET_DIR_PATH}/
# 清理
rm -rf ${TMP_DIR_PATH}

END_TIME=$(date +%s)

echo -e "耗时: $(expr ${END_TIME} - ${START_TIME})s"
echo -e "耗时: $((${END_TIME} - ${START_TIME}))s"
