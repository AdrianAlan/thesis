#!/bin/bash

echo "Debug: Checking for interfaces"
OVS_LIST=`sudo ovs-vsctl list interface | grep _uuid | awk '{print $3}'`
if [[ $OVS_LIST != "" ]]
then
	for INTERFACE in $OVS_LIST
	do	
		if [[ $1 == "true" ]]
		then		
			echo "Debug: Enabling BFD on $INTERFACE"
		else
			echo "Debug: Disabling BFD on $INTERFACE"
		fi
		sudo ovs-vsctl set interface $INTERFACE bfd:enable=$1
	done
else
	echo "Error: No Open vSwitches Found"
fi
