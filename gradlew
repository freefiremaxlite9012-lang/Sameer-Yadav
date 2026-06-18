#!/usr/bin/env sh

# Minimal gradlew script for GitHub Actions
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if [ -f "gradle/wrapper/gradle-wrapper.jar" ] ; then
    CLASSPATH="gradle/wrapper/gradle-wrapper.jar"
else
    echo "Error: gradle-wrapper.jar not found"
    exit 1
fi

exec "$JAVACMD" "-Xmx64m" "-Xms64m" -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
