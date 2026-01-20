#!/bin/bash

gradle --version
gradle build --continuous -x test &
gradle bootRun -Dspring-boot.run.jvmArguments="$JAVA_OPTS -Dspring.devtools.restart.enabled=true -Djava.net.preferIPv4Stack=true"