#!/bin/sh

ROOT=$(pwd)
ROOT=$(dirname "$0")

cd "$ROOT"
source config.build

if [[ "$TERM" != "dumb" ]]; then
    C0=$(tput sgr0)
    C1=$(tput setaf $(expr $BASE_COLOR + 72))
    C2=$(tput setaf $BASE_COLOR)
fi

printJob()
{
	echo ""
	echo $C2"#########################################"
	echo $C2"# "$C1$1
	echo $C2"#########################################"
	echo $C0
}

printJob "create folder"
sh create-folder.sh $LIB_NAME
printJob "copying jar"
sh copy_jar.sh $LIB_NAME
printJob "copying additional libs"
for i in ${ADDITIONAL_LIBS[@]}; do
	sh copy_additional_libs.sh $LIB_NAME $i
done
printJob "copying src"
sh copy_src.sh $LIB_NAME
printJob "copying README"
sh copy_readme.sh $LIB_NAME
printJob "copying reference"
sh copy_reference.sh $LIB_NAME
printJob "creating processing sketches"
for i in ${IO_EXAMPLE_PATHS[@]}; do
	sh create-processing-sketches.sh $LIB_NAME $i
done
printJob "packing zip"
sh pack-zip.sh $LIB_NAME
printJob "done"
