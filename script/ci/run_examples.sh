#!/bin/bash -e
#
# run the plugin examples to ensure plugin works as exemplified
# https://github.com/nkiraly
#

basedir=`pwd`

mvn_props=""

# if travis specified the binary location via environment variable
# then us it when calling the maven plugin
if [ -z "${TRAVIS_DBSTEWARD_BINARY_LOCATION}" ]; then
  mvn_props="" ;
else
  echo "Using environment variable TRAVIS_DBSTEWARD_BINARY_LOCATION ${TRAVIS_DBSTEWARD_BINARY_LOCATION} for project.dbsteward.path"
  mvn_props="-D project.dbsteward.path=${TRAVIS_DBSTEWARD_BINARY_LOCATION}" ;
fi


cd ${basedir}
cd example1
#mvn clean
mvn dbsteward-maven-plugin:sql-compile dbsteward-maven-plugin:db-create ${mvn_props}


cd ${basedir}
cd example2
#mvn clean
mvn dbsteward-maven-plugin:sql-diff dbsteward-maven-plugin:db-upgrade ${mvn_props}
