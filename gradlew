#!/usr/bin/env sh
##############################################################################
# Gradle start up script for UN*X
##############################################################################

##############################################################################
# (This is a standard Gradle wrapper script.)
##############################################################################

if [ -z "$(command -v dirname)" ]; then
  echo "dirname command not found" 1>&2
  exit 1
fi

PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done

PRG_DIR=`dirname "$PRG"`

exec "${PRG_DIR}/gradle/wrapper/gradle-wrapper.jar" "$@"
