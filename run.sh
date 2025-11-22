#!/bin/bash

# Production startup script with JVM arguments
# Usage: ./run.sh

java --enable-native-access=ALL-UNNAMED \
     -jar target/gradproject-0.0.1-SNAPSHOT.jar

