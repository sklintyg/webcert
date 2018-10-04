#!/bin/bash
# Assign backing service addresses from the outer environment

export DATABASE_USERNAME=${DATABASE_USERNAME:-webcert}
export DATABASE_PASSWORD=${DATABASE_PASSWORD:-webcert}
export DATABASE_NAME=${DATABASE_NAME:-webcert}
export DATABASE_SERVER=$MYSQL_SERVICE_HOST
export DATABASE_PORT=$MYSQL_SERVICE_PORT

export ACTIVEMQ_BROKER_USERNAME=${ACTIVEMQ_BROKER_USERNAME:-admin}
export ACTIVEMQ_BROKER_PASSWORD=${ACTIVEMQ_BROKER_PASSWORD:-admin}

export REDIS_PASSWORD=${REDIS_PASSWORD:-redis}
export REDIS_PORT=$REDIS_SERVICE_PORT
export REDIS_HOST=$REDIS_SERVICE_HOST

# dev profile is default for pipeline
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-test,wc-all-stubs,wc-security-test,testability-api,caching-enabled}

export CATALINA_OPTS_APPEND="\
-Dspring.session.redis.namespace=${APP_NAME} \
-Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
-Dconfig.file=/opt/$APP_NAME/config/webcert.properties \
-Dconfig.folder=/opt/$APP_NAME/config \
-Dlogback.file=classpath:logback-ocp.xml \
-Dcertificate.folder=/opt/$APP_NAME/certifikat \
-Dcredentials.file=/opt/$APP_NAME/env/secret-env.properties \
-Dresources.folder=/tmp/resources \
-Dfile.encoding=UTF-8 \
-DbaseUrl=http://${APP_NAME}:8080"
