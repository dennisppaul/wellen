#!/bin/sh

LIB_NAME=$1
EXTRA_LIB_NAME=$2
ROOT=$(pwd)

SRC=$ROOT/../resources/$EXTRA_LIB_NAME
DST=$ROOT/../processing-library/$LIB_NAME/resources/

echo "# resource '"$EXTRA_LIB_NAME"'"
cp "$SRC" "$DST"
