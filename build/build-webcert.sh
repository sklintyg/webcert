#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/webcert
mvn install
