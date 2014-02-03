#!/usr/local/bin/bash
#
# run the plugin examples to ensure plugin works as exemplified
# https://github.com/nkiraly
#

basedir=`pwd`

cd ${basedir}
cd example1
mvn sql-compile db-create

cd ${basedir}
cd example2
mvn sql-diff db-upgrade
