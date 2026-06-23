#!/bin/bash
# build_android.sh — WSL gradle build wrapper
# Converts Linux paths to Windows paths for Windows JDK

export JAVA_HOME="$HOME/jdk17"
PROJECT_DIR="/mnt/c/Users/13342/Desktop/SmartTravelApp/SmartTravelApp"

# Convert project dir to Windows path
WIN_PROJECT_DIR=$(echo "$PROJECT_DIR" | sed 's|/mnt/c/|C:\\|' | sed 's|/|\\\\|g')

cd "$PROJECT_DIR"

JAVA_CMD="$JAVA_HOME/bin/java.exe"
JAR_PATH="$PROJECT_DIR/gradle/wrapper/gradle-wrapper.jar"

# Convert JAR path to Windows path
WIN_JAR_PATH=$(echo "$JAR_PATH" | sed 's|/mnt/c/|C:\\|' | sed 's|/|\\\\|g')

echo "JAVA: $JAVA_CMD"
echo "JAR: $WIN_JAR_PATH"

"$JAVA_CMD" \
  -Dorg.gradle.appname=gradlew \
  -classpath "$WIN_JAR_PATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
