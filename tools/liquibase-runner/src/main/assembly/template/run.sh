#!/bin/sh
java -jar lib/webcert-liquibase-runner-jar-with-dependencies.jar --changeLogFile="changelog/changelog.xml" --contexts=none update
