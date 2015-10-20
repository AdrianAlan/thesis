#!/bin/bash
# Run ONOS and REST API
# > whitetst <topo> <source host> <destination host> <interval> <interface a> <interface b>
URL="http://localhost:5555"

echo "*** Running White Test"
echo "*** Cleaning..."
SUCCESS=`curl $URL/clean`
if [[ $SUCCESS == "Success " ]]
then
    echo "*** Starting Mininet"
    SUCCESS=`curl $URL/start/$1`
    if [[ $SUCCESS == "Success " ]]
    then
	echo "*** Test ping"
	SUCCESS=`curl $URL/ping/$2/$3/0.1`
	if [[ $SUCCESS != "" ]]
	then
	    echo "*** Testing..."
	    DROPPED=`curl $URL/ping/$2/$3/$4 &`
	    sleep 0.01
	    echo "*** Link down"
	    SUCCESS=`curl $URL/link/$5/$6/down`
	    if [[ $DROPPED != "" ]]
	    then
		echo "*** RESULTED IN"
		echo $DROPPED
	    fi
	else
	    echo "*** Terminated 3"
	fi	    
    else
	echo "*** Terminated 2"
    fi
else
    echo "*** Terminated 1"
fi
