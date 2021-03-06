#!/bin/sh

#Scripts that creates replication privilegdes for the slave db to the master.

# Set default home if not already set.
SYSCHECK_HOME=${SYSCHECK_HOME:-"/usr/local/syscheck"}

## Import common definitions ##
. $SYSCHECK_HOME/resources.sh

SCRIPTID=801
getlangfiles $SCRIPTID || exit 1;
getconfig $SCRIPTID || exit 1;

ERRNO_1="${SCRIPTID}1"
ERRNO_2="${SCRIPTID}2"
ERRNO_3="${SCRIPTID}3"
ERRNO_4="${SCRIPTID}4"



PRINTTOSCREEN=1

if [ "x$1" = "x-h" -o "x$1" = "x--help" ] ; then
	help
elif [ "x$1" = "x-s" -o  "x$1" = "x--screen" -o \
    "x$2" = "x-s" -o  "x$2" = "x--screen"   ] ; then
	PRINTTOSCREEN=1
fi 


help () {
        echo "$HELP"
        echo "$ERRNO_1/$DESCR_1 - $HELP_1"
        echo "$ERRNO_2/$DESCR_2 - $HELP_2"
        echo "$ERRNO_3/$DESCR_3 - $HELP_3"
        echo "$ERRNO_4/$DESCR_4 - $HELP_4"
        echo "${SCREEN_HELP}"
        exit
}


echo "are you really sure you want to drop and replace the ejbca db on this host?"
echo "enter 'im-really-sure' to continiue or ctrl-c to abort"
read a
if [ "x$a" != "xim really sure" ] ; then
	echo "ok probably wise choice, exiting"
	exit
fi


if [ -x $SYSCHECK_HOME/related-enabled/904_make_mysql_db_backup.sh ] ; then
	$SYSCHECK_HOME/related-enabled/904_make_mysql_db_backup.sh -s
else
	printlogmess $LEVEL_3 $ERRNO_3 "$DESCR_3"
	exit
fi
	
$MYSQLADMIN_BIN drop $DB_NAME -u root --password="$MYSQLROOT_PASSWORD" 
if [ $? -eq 0 ] ; then
	printlogmess $LEVEL_1 $ERRNO_1 "$DESCR_1"
else
	printlogmess $LEVEL_2 $ERRNO_2 "$DESCR_2" "$?"
fi

