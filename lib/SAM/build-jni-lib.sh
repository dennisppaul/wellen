#!/bin/bash

JAVA_SRC_PATH='../../src/'
NATIVE_ACCESSER_PATH='wellen/SAM.java'
NATIVE_ACCESSER_CLASS_NAME='wellen.SAM'
NATIVE_HEADER_PATH='../cpp/'
PWD=`pwd`

echo
echo 'Wellen SAM JNI Builder'
# echo 'Options:'
# echo '--refresh-header     Refreshes the header'
# echo '--build              Tries CMake build'
# echo '--execute-java       Tests library'
echo

refresh_header () {
    cd java/
    javac -h $NATIVE_HEADER_PATH $NATIVE_ACCESSER_PATH
    cd ../
    echo "Generation finished."
}

buildj () {
    rm -rf build
    mkdir build
    cd build
    cmake ../
    make
    ls
    cd ../
    echo 'Build finished.'
}

exej () {
    cd java/
    javac $NATIVE_ACCESSER_PATH
    java $NATIVE_ACCESSER_CLASS_NAME -D$PWD/../build
    cd ../
}

cp $JAVA_SRC_PATH/$NATIVE_ACCESSER_PATH java/$NATIVE_ACCESSER_PATH

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

mv ./build/libjni_wellen_sam.dylib ../ # move library to lib folder

rm java/$NATIVE_ACCESSER_PATH