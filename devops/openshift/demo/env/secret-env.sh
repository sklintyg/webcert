#!/bin/bash

export CATALINA_OPTS_APPEND="\
-Dspring.session.redis.namespace=${APP_NAME} \
-Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
-Dconfig.file=/opt/$APP_NAME/config/webcert.properties \
-Dconfig.folder=/opt/$APP_NAME/config \
-Dlogback.file=/opt/$APP_NAME/config/logback-ocp.xml \
-Dcertificate.folder=/opt/$APP_NAME/certifikat \
-Dcredentials.file=/opt/$APP_NAME/env/secret-env.properties \
-Dresources.folder=/tmp/resources \
-Dfile.encoding=UTF-8 \
-DbaseUrl=http://${APP_NAME}:8080"
