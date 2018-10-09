#!/bin/bash
#
# Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
# and other contributors as indicated by the @author tags.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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