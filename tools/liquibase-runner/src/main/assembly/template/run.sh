#!/bin/sh
java -jar lib/${project.build.finalName}.jar --logLevel=info --changeLogFile="changelog/changelog.xml" --contexts=none update
