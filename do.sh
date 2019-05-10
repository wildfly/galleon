#!/bin/bash
#
# Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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

echo=off
BUILD=package
RUN=run
DEBUG_MODE="${DEBUG:-false}"
DEBUG_PORT="${DEBUG_PORT:-8787}"
GREP="grep"

while [ "$#" -gt 0 ]
do
    case "$1" in
      --debug)
          DEBUG_MODE=true
          if [ -n "$2" ] && [ "$2" = `echo "$2" | sed 's/-//'` ]; then
              DEBUG_PORT=$2
              shift
          fi
          ;;
    build)
          unset RUN
          ;;
     run)
          unset BUILD
          ;;
     *)
          ARGS="$ARGS $1"
          ;;
    esac
    shift
done

# Set debug settings if not already set
if [ "$DEBUG_MODE" = "true" ]; then
    DEBUG_OPT=`echo $JAVA_OPTS | $GREP "\-agentlib:jdwp"`
    if [ "x$DEBUG_OPT" = "x" ]; then
        JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=n"
    else
        echo "Debug already enabled in JAVA_OPTS, ignoring --debug argument"
    fi
fi

if [[ -n $BUILD ]]; then
    mvn clean install
fi

# Set default modular JVM options
java --add-modules=java.se -version > /dev/null 2>&1 && MODULAR_JDK=true || MODULAR_JDK=false
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

if [[ -n $RUN ]]; then
  LOG_CONF=`echo $JAVA_OPTS | grep "logging.configuration"`
  if [ "x$LOG_CONF" = "x" ]; then
    java $JAVA_OPTS -Dlogging.configuration=file:"./content/bin/galleon-cli-logging.properties" -jar ./cli/target/galleon-cli-4.0.1.Alpha1-SNAPSHOT.jar $ARGS
  else
    echo "logging.configuration already set in JAVA_OPTS"
    java $JAVA_OPTS -jar ./cli/target/galleon-cli-4.0.1.Alpha1-SNAPSHOT.jar $ARGS
  fi
fi
