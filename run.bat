@echo off
REM Production startup script for Windows with JVM arguments
REM Usage: run.bat

java --enable-native-access=ALL-UNNAMED -jar target\gradproject-0.0.1-SNAPSHOT.jar

