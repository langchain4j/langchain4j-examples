#!/bin/bash
export JDK21_HOME=/usr/local/opt/openjdk/libexec/openjdk.jdk/Contents/Home
#export JDK17_HOME=/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
#export JDK11_HOME=/usr/local/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home
#export JDK8_HOME=/usr/local/opt/openjdk@8/libexec/openjdk.jdk/Contents/Home

export JAVA_HOME=$JDK21_HOME
mvn install -DskipTests -Denforcer.skip=true
