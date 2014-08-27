#!/bin/sh
java -cp .:lib/${project.build.finalName}.jar se.inera.certificate.liquibase.Runner
