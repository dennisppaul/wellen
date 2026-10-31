#!/usr/bin/env bash

set -e

source config.build

#####################################################

LIB_NAME=$1
DIR_NAME=resources
FILE_LIST=${RESOURCES[@]}

#####################################################

ROOT=$(pwd)

SRC=$ROOT/../$DIR_NAME
DST=$ROOT/../processing-library/$LIB_NAME/$DIR_NAME/

if [ -d "$DST" ]; then
	rm -rf "$DST"
fi
mkdir -p "$DST"

echo "# copying "$DIR_NAME":"
for i in $FILE_LIST; do
    echo "# '"$i"'"
    cp "$SRC/$i" "$DST"
done
