#!/bin/sh
java -jar lib/webcert-liquibase-runner-0-SNAPSHOT-jar-with-dependencies.jar --changeLogFile="changelog/changelog.xml" --contexts=none update
