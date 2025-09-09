#!/bin/bash

gradle --version
gradle build --continuous -x test &
gradle bootRun -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"