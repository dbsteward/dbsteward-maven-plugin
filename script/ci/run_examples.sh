#!/bin/bash -e
#
# run the plugin examples to ensure plugin works as exemplified
# https://github.com/nkiraly
#

basedir=`pwd`

cd ${basedir}
cd example1
mvn dbsteward-maven-plugin:sql-compile dbsteward-maven-plugin:db-create

cd ${basedir}
cd example2
mvn dbsteward-maven-plugin:sql-compile dbsteward-maven-plugin:db-create
