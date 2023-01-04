#! /bin/zsh

PROCESSING_CLI=/usr/local/bin/processing-java
OUTPUT_PATH=/tmp/wellen/
TIMEOUT=/usr/local/bin/timeout
TEST_DURATION=8
FAILURE_COUNTER=0
EXAMPLE_FOLDER_PATH=../processing-library/wellen/examples/
DO_COMPILE_TEST=0
DO_RUN_TEST=0

###############################################################################

OPT_COMPILE="COMPILE"
OPT_RUN="RUN"
BASE_COLOR=23

if [[ "$TERM" != "dumb" ]]; then
    COLOR_NORM=$(tput sgr0)
    COLOR_OK=$(tput setaf 2)
    COLOR_FAIL=$(tput setaf 1)
fi

###############################################################################

print_section() {
    echo
    echo "++++++++++++++++++++++++++++++++++++++++++"
    echo "+++ "$1 | tr a-z A-Z
    echo "++++++++++++++++++++++++++++++++++++++++++"
    echo
}

run_test() {
    P_OPTION=$2
    P_TIMEOUT=$3
    P_PATH_TO_SKETCH=$(dirname $1)

	if [[ "$P_OPTION" == $OPT_COMPILE ]]; then
	   echo -n $OPT_COMPILE
	else
	   echo -n $OPT_RUN
	fi
	echo -n ":"
	
	if [[ "$P_OPTION" == $OPT_COMPILE ]]; then
		$PROCESSING_CLI --sketch=$P_PATH_TO_SKETCH --output=$OUTPUT_PATH --force --build > /dev/null 2>&1
		RESULT_VALUE=$?
		OK_VALUE=0
	else
		timeout --preserve-status $P_TIMEOUT $PROCESSING_CLI --sketch=$P_PATH_TO_SKETCH --output=$OUTPUT_PATH --force --run > /dev/null 2>&1
		RESULT_VALUE=$?
		OK_VALUE=143
	fi

	if [ $RESULT_VALUE -eq $(($OK_VALUE)) ]; then
	   echo -n $COLOR_OK"OK"
	else
	   echo -n $COLOR_FAIL"FAIL"
	   FAILURE_COUNTER=$((FAILURE_COUNTER+1));
	fi
	echo -n $COLOR_NORM
}

###############################################################################

get_abs_filename() {
  # $1 : relative filename
  echo "$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
}

###############################################################################

test_library_examples() {
	for FILE in **/*.pde
	do
		FULL_PATH=$(realpath $FILE)
		FILE_NAME=$(basename $FILE)
		echo    "+++ TEST '"$FILE_NAME"'"
		echo -n "    "
		if [ "$DO_COMPILE_TEST" -ge 1 ]
		then
# 			echo -n "COMPILE"
	 		run_test $FULL_PATH $OPT_COMPILE $TEST_DURATION
		fi
		if [ "$DO_COMPILE_TEST" -ge 1 ] && [ "$DO_RUN_TEST" -ge 1 ]
		then
			echo -n " + "
		fi
		if [ "$DO_RUN_TEST" -ge 1 ]
		then
# 			echo -n "RUN"
	 		run_test $FULL_PATH $OPT_RUN     $TEST_DURATION
		fi
 		echo
	done
}

###############################################################################

print_section "check for Processing CLI"

IS_TOOL_PRESENT=$(command -v $PROCESSING_CLI)
if [ -z "$IS_TOOL_PRESENT" ]
then
    echo "    ERROR '"$PROCESSING_CLI"' is not installed."
    echo "    this can be done from within the Processing.app under 'Tools > Install \"processing-java\"'"
    exit -1
else
    echo "    OK"
fi

###############################################################################

if [[ $# -eq 0 ]]; then
	DO_COMPILE_TEST=1
	DO_RUN_TEST=1
else
	while getopts ":rc" opt; do
		case $opt in
		r)
			DO_RUN_TEST=1
			;;
		c)
			DO_COMPILE_TEST=1
			;;
		\?)
			echo "+++ invalid flag: -$OPTARG"
			echo "+++ use -c to compile examples, -r to run examples, or no flags to do both"
			exit -3
			;;
		esac
	done
fi

###############################################################################

print_section "compile and run examples"

M_PATH=$(get_abs_filename "")

cd $M_PATH/$EXAMPLE_FOLDER_PATH/
echo "+++"
echo "+++ FOLDER: '"$(realpath $M_PATH/$EXAMPLE_FOLDER_PATH)"'"
echo "+++"
test_library_examples
cd $M_PATH

if [ "$FAILURE_COUNTER" -eq 0 ]
then
	echo "+++ ALL EXAMPLES TESTED "$COLOR_OK"OK"$COLOR_NORM
	exit 0
else
	echo "+++ SOME EXAMPLES "$COLOR_FAIL"FAILED"$COLOR_NORM
	echo "+++ NUMBER OF EXAMPLES FAILED:" $FAILURE_COUNTER
	exit -2
fi