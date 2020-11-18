#!/bin/sh

LIB_NAME=$1
ROOT=$(pwd)

DST=$ROOT/../processing-library/$LIB_NAME/resources/

source config.build

if [ -d "$DST" ]; then
	rm -rf "$DST"
fi
mkdir -p "$DST"

for i in ${RESOURCES[@]}; do
	sh copy_resource.sh $LIB_NAME $i
done
