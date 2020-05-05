#!/bin/sh
PWD=`pwd`
RESOURCE_NAME=`find ./ -name *.jar`
RESOURCE_NAME=$PWD/$RESOURCE_NAME

tpid=`ps -ef|grep $RESOURCE_NAME|grep -v grep|grep -v kill|awk '{print $2}'`
if [ ${tpid} ]; then
echo 'Stop Process...'
kill -15 $tpid
fi
