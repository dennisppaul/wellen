#!/bin/sh

LIB_NAME=$1
ROOT=$(pwd)

SRC=$ROOT/../lib/$LIB_NAME.jar
DST=$ROOT/../processing-library/$LIB_NAME/library/

# delete unwanted directories from jar 
#zip --quiet --delete $SRC wellen_SAM.h wellen_SAM_impl.h wellen_SAM_impl.cpp "wellen/tests/*" "wellen/examples/*"

if [ -d "$DST" ]; then
	rm -rf "$DST"
fi
mkdir -p "$DST"

cp "$SRC" "$DST"
