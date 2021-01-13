#!/bin/sh

LIB_NAME=$1
ROOT=$(pwd)

SRC=$ROOT/../resources/$ADDITIONAL_LIB_NAME
DST=$ROOT/../processing-library/$LIB_NAME/resources/

source config.build

if [ -d "$DST" ]; then
	rm -rf "$DST"
fi
mkdir -p "$DST"

for i in ${RESOURCES[@]}; do
    echo "# resource '"$i"'"
    cp $SRC/$i $DST
done
