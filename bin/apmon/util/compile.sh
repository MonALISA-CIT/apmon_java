#!/bin/sh

if [ -z "${JAVA_HOME}" ]; then
    echo "Please set the JAVA_HOME environment variable."
    exit 1
fi

if gcc -fPIC -shared -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux \
      NativeLinux.c -o libnativeapm.so; then
      exit 0
else
    exit 1
fi;

