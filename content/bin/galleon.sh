#!/bin/bash
#
# Copyright The WildFly Authors
# SPDX-License-Identifier: Apache-2.0
#

DIRNAME=`dirname "$0"`
echo=off
DEBUG_MODE="${DEBUG:-false}"
DEBUG_PORT="${DEBUG_PORT:-8787}"
GREP="grep"

# Set debug settings if not already set
if [ "$DEBUG_MODE" = "true" ]; then
    DEBUG_OPT=`echo $JAVA_OPTS | $GREP "\-agentlib:jdwp"`
    if [ "x$DEBUG_OPT" = "x" ]; then
        JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=n"
    else
        echo "Debug already enabled in JAVA_OPTS, ignoring --debug argument"
    fi
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

$JAVA --add-modules=java.se -version > /dev/null 2>&1 && MODULAR_JDK=true || MODULAR_JDK=false

if [ "$MODULAR_JDK" = "true" ]; then
  DEFAULT_MODULAR_JVM_OPTIONS=`echo $* | $GREP "\-\-add\-modules"`
  if [ "x$DEFAULT_MODULAR_JVM_OPTIONS" = "x" ]; then
    # Set default modular jdk options
    DEFAULT_MODULAR_JVM_OPTIONS="$DEFAULT_MODULAR_JVM_OPTIONS --add-modules=java.se"
  else
    DEFAULT_MODULAR_JVM_OPTIONS=""
  fi
fi

JAVA_OPTS="$JAVA_OPTS $DEFAULT_MODULAR_JVM_OPTIONS"

LOG_CONF=`echo $JAVA_OPTS | grep "logging.configuration"`
if [ "x$LOG_CONF" = "x" ]; then
  exec "$JAVA" $JAVA_OPTS -Dlogging.configuration=file:"$DIRNAME"/galleon-cli-logging.properties -jar "$DIRNAME"/galleon-cli.jar "$@"
else
  echo "logging.configuration already set in JAVA_OPTS"
  exec "$JAVA" $JAVA_OPTS -jar "$DIRNAME"/galleon-cli.jar "$@"
fi