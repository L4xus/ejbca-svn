#!/bin/sh

#Scripts that creates replication privilegdes for the slave db to the master.

# Set default home if not already set.
SYSCHECK_HOME=${SYSCHECK_HOME:-"/usr/local/syscheck"}

## Import common definitions ##
. $SYSCHECK_HOME/resources.sh

SCRIPTID=810
getlangfiles $SCRIPTID || exit 1;
getconfig $SCRIPTID || exit 1;

ERRNO_1="${SCRIPTID}1"
ERRNO_2="${SCRIPTID}2"

schelp () {
        echo "$HELP"
        echo "$ERRNO_1/$DESCR_1 - $HELP_1"
        echo "$ERRNO_2/$DESCR_2 - $HELP_2"
        echo "${SCREEN_HELP}"
        exit
}

PRINTTOSCREEN=1

if [ "x$1" = "x-h" -o "x$1" = "x--help" ] ; then
        schelp
elif [ "x$1" = "x-s" -o  "x$1" = "x--screen" -o \
    "x$2" = "x-s" -o  "x$2" = "x--screen"   ] ; then
        PRINTTOSCREEN=1
elif [ "x$1" = "x-q" -o  "x$1" = "x--quiet" -o \
    "x$2" = "x-q" -o  "x$2" = "x--quiet"   ] ; then
        PRINTTOSCREEN=0
fi 



echo "SHOW MASTER STATUS;" | $MYSQL_BIN mysql -u root --password="$MYSQLROOT_PASSWORD" 
if [ $? -eq 0 ] ; then
        printlogmess $LEVEL_1 $ERRNO_1 "$DESCR_1"
else
        printlogmess $LEVEL_2 $ERRNO_2 "$DESCR_2"
fi

echo "SHOW PROCESSLIST \G;" | $MYSQL_BIN mysql -u root --password="$MYSQLROOT_PASSWORD" 
if [ $? -eq 0 ] ; then
        printlogmess $LEVEL_1 $ERRNO_1 "$DESCR_1"
else
        printlogmess $LEVEL_2 $ERRNO_2 "$DESCR_2"
fi

echo "SHOW SLAVE HOSTS; " | $MYSQL_BIN mysql -u root --password="$MYSQLROOT_PASSWORD" 
if [ $? -eq 0 ] ; then
        printlogmess $LEVEL_1 $ERRNO_1 "$DESCR_1"
else
        printlogmess $LEVEL_2 $ERRNO_2 "$DESCR_2"
fi

