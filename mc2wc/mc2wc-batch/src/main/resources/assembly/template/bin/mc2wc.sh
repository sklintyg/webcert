#!/bin/sh
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"
pushd $DIR
COMMAND_STRING="java -DconfigFile=$DIR/conf/prod.properties -DlogFile=log/ -DexportDir=$DIR/export/ -jar lib/mc2wc.one-jar.jar -e"
eval $COMMAND_STRING
popd


