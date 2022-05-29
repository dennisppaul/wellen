#!/bin/sh

source config.build

#####################################################

LIB_NAME=$1
DIR_NAME=tools
FILE_LIST=${TOOLS[@]}

#####################################################

ROOT=$(pwd)

SRC=$ROOT/../$DIR_NAME/$ADDITIONAL_LIB_NAME
DST=$ROOT/../processing-library/$LIB_NAME/$DIR_NAME/

if [ -d "$DST" ]; then
	rm -rf "$DST"
fi
mkdir -p "$DST"

echo "# copying "$DIR_NAME":"
for i in $FILE_LIST; do
    echo "# '"$i"'"
    cp $SRC/$i $DST
done
