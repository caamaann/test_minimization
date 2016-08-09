#!/bin/bash

USAGE="Usage (only works with grep linux): bash find_name_testcases <path_test_directory>"


if [ "$#" -lt "1" ]; then
    echo $USAGE
    exit -1
fi


APP_TEST_DIR=$1
#APP_TEST_DIR="test/jdepend/framework/"

GREP_COMMAND="grep -r -H -o -e '\test[[:alnum:]]*[\(]' $APP_TEST_DIR | grep -o -e '[[:alnum:]]*[.][[:alnum:]]*[:]\test[[:alnum:]]*'"
#grep -r -H -o -e '\test[[:alnum:]]*[\(]' $APP_TEST_DIR | grep -o -e '[[:alnum:]]*[.][[:alnum:]]*[:]\test[[:alnum:]]*'

eval $GREP_COMMAND
