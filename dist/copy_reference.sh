#!/bin/sh

LIB_NAME=$1
ROOT=$(pwd)

# update stylessheet from lib
CSS=stylesheet.css
SRC=$ROOT/../lib/$CSS
DST=$ROOT/../reference
cp "$SRC" "$DST"

# copy reference

SRC=$ROOT/../reference
DST=$ROOT/../processing-library/$LIB_NAME/

cp -r "$SRC" "$DST"

