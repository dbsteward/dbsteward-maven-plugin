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


# example 1 and 2 create the database from zero and upgrade it, so drop the someapp db before doing example 1
PGPASSWORD=password1 psql -U dbsteward_ci -d postgres -c "DROP DATABASE someapp;"


cd ${basedir}  || exit 100
cd example1  || exit 101
mvn clean  || exit 102
mvn dbsteward:sql-compile dbsteward:db-create ${mvn_props}  || exit 110


cd ${basedir}  || exit 120
cd example2  || exit 121
mvn clean  || exit 122
mvn dbsteward:sql-diff dbsteward:db-upgrade ${mvn_props}  || exit 130

