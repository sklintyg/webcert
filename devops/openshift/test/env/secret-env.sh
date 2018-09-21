#!/bin/bash
# Assign backing service addresses from the outer environment

export DATABASE_USERNAME=${DATABASE_USERNAME:-intyg}
export DATABASE_PASSWORD=${DATABASE_PASSWORD:-intyg}
export DATABASE_NAME=${DATABASE_NAME:-webcert_test}
export DATABASE_SERVER=$MYSQL_SERVICE_HOST
export DATABASE_PORT=$MYSQL_SERVICE_PORT

export ACTIVEMQ_BROKER_USERNAME=${ACTIVEMQ_BROKER_USERNAME:-admin}
export ACTIVEMQ_BROKER_PASSWORD=${ACTIVEMQ_BROKER_PASSWORD:-admin}

export REDIS_PASSWORD=${REDIS_PASSWORD:-redis}
export REDIS_PORT=$REDIS_SERVICE_PORT
export REDIS_HOST=$REDIS_SERVICE_HOST

# dev profile is default for pipeline
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev,caching-enabled}

export CATALINA_OPTS_APPEND="\
-Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
-Dwebcert.config.file=/opt/$APP_NAME/config/webcert.properties \
-Dwebcert.logback.file=classpath:logback-ocp.xml \
-Dcertificate.folder=/opt/$APP_NAME/env \
-Dcredentials.file=/opt/$APP_NAME/env/secret-env.properties \
-Dwebcert.resources.folder=/tmp/resources \
-Dfile.encoding=UTF-8 \
-DbaseUrl=http://${APP_NAME}:8080 \
-Dintygstjanst.base.url=http://intygstjanst-test-${APP_NAME}:8080"
