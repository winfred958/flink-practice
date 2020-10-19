#!/bin/bash

#######################################
## python path
#######################################
PROJECT_ROOT_PATH=${1}
export PYTHONPATH=${PROJECT_ROOT_PATH}:$PYTHONPATH
