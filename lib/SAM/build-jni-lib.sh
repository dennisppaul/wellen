#!/bin/bash

JAVA_SRC_PATH='../../src/'
NATIVE_ACCESSER_PATH='wellen/SAM.java'
NATIVE_ACCESSER_CLASS_NAME='wellen.SAM'
NATIVE_HEADER_PATH='../cpp/'
PWD=`pwd`
BUILD_PATH=build
JAVA_BUILD_PATH=java

echo
echo '----- Wellen SAM JNI Builder -----'
# echo 'Options:'
# echo '--refresh-header     Refreshes the header'
# echo '--build              Tries CMake build'
# echo '--execute-java       Tests library'
echo

refresh_header () {
    cd $JAVA_BUILD_PATH
    javac -h $NATIVE_HEADER_PATH $NATIVE_ACCESSER_PATH
    cd ../
    echo "Generation finished."
}

buildj () {
    rm -rf $BUILD_PATH
    mkdir $BUILD_PATH
    cd $BUILD_PATH
    cmake ../
    make
    cd ../
    echo 'Build finished.'
}

exej () {
    cd $JAVA_BUILD_PATH
    javac $NATIVE_ACCESSER_PATH
    java $NATIVE_ACCESSER_CLASS_NAME -D$PWD/../build
    cd ../
}

mkdir -p $JAVA_BUILD_PATH/wellen
cp $JAVA_SRC_PATH/$NATIVE_ACCESSER_PATH $JAVA_BUILD_PATH/$NATIVE_ACCESSER_PATH
cp $JAVA_SRC_PATH/wellen/DSPNodeOutput.java $JAVA_BUILD_PATH/wellen/ # @TODO

if [ "$1" != "" ]; then
    while [ "$1" != "" ]; do
        case $1 in
            --refresh-header        )   refresh_header
                                        ;;
            --build                 )   buildj
                                        ;;
            --execute-java | --exej )   exej
        esac
        shift
    done
else
    refresh_header
    buildj
    exej
fi

# move library to lib folder
MACOS_DYLIB=libjni_wellen_sam.dylib
if [ -f "$BUILD_PATH/$MACOS_DYLIB" ]; then
    mv $BUILD_PATH/$MACOS_DYLIB ../ 
    echo "copying library: "$BUILD_PATH/$MACOS_DYLIB
fi
LINUX_DYLIB=libjni_wellen_sam.so
if [ -f "$BUILD_PATH/$LINUX_DYLIB" ]; then
    echo "copying library: "$BUILD_PATH/$LINUX_DYLIB
    mv $BUILD_PATH/$LINUX_DYLIB ../ 
fi

rm -rf $JAVA_BUILD_PATH
rm -rf $BUILD_PATH
