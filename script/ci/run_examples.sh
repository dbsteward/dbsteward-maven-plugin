#!/bin/bash -e
#
# run the plugin examples to ensure plugin works as exemplified
# https://github.com/nkiraly
#

basedir=`pwd`

mvn_props=""

# if travis specified the binary location via environment variable
# then us it when calling the maven plugin
if [ -z "${DBSTEWARD_BINARY_LOCATION}" ]; then
  mvn_props="" ;
else
  echo "Using environment variable DBSTEWARD_BINARY_LOCATION ${DBSTEWARD_BINARY_LOCATION} for project.dbsteward.path"
  mvn_props=" -D project.dbsteward.path=${DBSTEWARD_BINARY_LOCATION} " ;
fi

if [ -z "${DBSTEWARD_SQLFORMAT}" ]; then
  mvn_props+="" ;
else
  echo "Using environment variable DBSTEWARD_SQLFORMAT ${DBSTEWARD_SQLFORMAT} for project.dbsteward.sqlFormat"
  mvn_props+=" -D project.dbsteward.sqlFormat=${DBSTEWARD_SQLFORMAT} " ;
fi

psql -U dbsteward_ci -d postgres -c "DROP DATABASE someapp;"


cd ${basedir}  || exit 100
cd example1  || exit 101
#mvn clean  || exit 102
mvn dbsteward-maven-plugin:sql-compile dbsteward-maven-plugin:db-create ${mvn_props}  || exit 110


cd ${basedir}  || exit 150
cd example2  || exit 151
#mvn clean  || exit 152
mvn dbsteward-maven-plugin:sql-diff dbsteward-maven-plugin:db-upgrade ${mvn_props}  || exit 160
