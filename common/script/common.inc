#!/bin/sh

set -e
SOURCE_CODE_DIR=$(pwd)
JAVA_OPTS='-Djdk.tls.namedGroups="secp256r1,secp256k1"'
function check_jdk()
{
    echo "Begin to check jdk..."
    # Determine the Java command to use to start the JVM.
    if [ -n "$JAVA_HOME" ] ; then
        if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
            # IBM's JDK on AIX uses strange locations for the executables
            JAVACMD="$JAVA_HOME/jre/sh/java"
        else
            JAVACMD="$JAVA_HOME/bin/java"
    fi
        if [ ! -x "$JAVACMD" ] ; then
            echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME
                Please set the JAVA_HOME variable in your environment to match the
                location of your Java installation."
        fi
    else
        which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

    Please set the JAVA_HOME variable in your environment to match the
    location of your Java installation."
    fi
    echo "jdk check finished."
}

function build_classpath()
{

    CLASSPATH=${SOURCE_CODE_DIR}/resources
    for jar_file in ${SOURCE_CODE_DIR}/dist/app/*.jar
        do
            CLASSPATH=${CLASSPATH}:${jar_file}
        done

    for jar_file in ${SOURCE_CODE_DIR}/dist/lib/*.jar
        do
            CLASSPATH=${CLASSPATH}:${jar_file}
        done
}