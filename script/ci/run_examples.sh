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

# kill all slons to make sure cluster remnants are stopped
killall slon

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


# test that example 3 will make the master and replica databases on its own just with the goal slony-install specified
# drop all dbs that example 3 will make or replicate to
PGPASSWORD=password1 psql -U dbsteward_ci -d postgres -c "DROP DATABASE someapp;"
PGPASSWORD=password1 psql -U dbsteward_ci -d postgres -c "DROP DATABASE someapp_b;"
PGPASSWORD=password1 psql -U dbsteward_ci -d postgres -c "DROP DATABASE someapp_c;"

# kill all slons to make sure cluster remnants are stopped
killall slon

cd ${basedir}  || exit 140
cd example3  || exit 141
mvn clean  || exit 142
# start slons that example 3 will depend on before engaging slony install
slon -f slon/slon-someapp.conf   -p slon/someapp.pid   > slon/someapp.log   2>&1 &
slon -f slon/slon-someapp_b.conf -p slon/someapp_b.pid > slon/someapp_b.log 2>&1 &
slon -f slon/slon-someapp_c.conf -p slon/someapp_c.pid > slon/someapp_c.log 2>&1 &
# if you want to watch what the slons are doing, tail with something like
# tail -F someapp*.log
mvn dbsteward:slony-install ${mvn_props}  || exit 150


# example 3 and 4 create the database from zero and install and upgrade via slony
# so drop the someapp db before doing example 3

# drop all dbs that example 4 will make or replicate to
PGPASSWORD=password1 psql -U dbsteward_ci -d postgres -c "DROP DATABASE someapp;"
PGPASSWORD=password1 psql -U dbsteward_ci -d postgres -c "DROP DATABASE someapp_b;"
PGPASSWORD=password1 psql -U dbsteward_ci -d postgres -c "DROP DATABASE someapp_c;"

# kill all slons to make sure cluster remnants are stopped
killall slon

# create database and install slony
cd ${basedir}  || exit 160
cd example3  || exit 161
mvn clean  || exit 162
# start slons that example 3 will depend on before engaging slony install
slon -f slon/slon-someapp.conf   -p slon/someapp.pid   > slon/someapp.log   2>&1 &
slon -f slon/slon-someapp_b.conf -p slon/someapp_b.pid > slon/someapp_b.log 2>&1 &
slon -f slon/slon-someapp_c.conf -p slon/someapp_c.pid > slon/someapp_c.log 2>&1 &
# if you want to watch what the slons are doing, tail with something like
# tail -F someapp*.log
mvn dbsteward:sql-compile dbsteward:db-create dbsteward:slony-install ${mvn_props}  || exit 170

# upgrade slony replicated database to example4
cd ${basedir}  || exit 180
cd example3  || exit 181
mvn clean  || exit 182
mvn dbsteward:slony-upgrade ${mvn_props}  || exit 190

