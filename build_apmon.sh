#!/bin/sh

if ! [ -d lib ]; then
    mkdir lib
fi

if [ -d /usr/lib/jvm/default-java/include ]; then
    export JAVA_HOME=${JAVA_HOME:=/usr/lib/jvm/default-java}
fi

cd src/apmon/util
if ./compile.sh; then
    mv libnativeapm.so ../../../lib
    cd ../../..
else
    echo "There was an error while building the native library"
    cd ../../..
    exit 1
fi

CRT_DIR=`pwd`
export CLASSPATH=$CLASSPATH:${CRT_DIR}

mkdir -p build/classes

cd src

find apmon -name \*.java | xargs javac -g -d ../build/classes

mkdir -p ../build/classes/apmon/lisa_host/Windows
cp apmon/lisa_host/Windows/*.dll ../build/classes/apmon/lisa_host/Windows/

cd ../build/classes

find . -type f | xargs jar cf apmon.jar
mv apmon.jar ../../lib

#javac examples/*.java
