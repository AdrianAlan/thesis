#!/bin/bash
# usage test.sh <interface> <count> <dst_ip> <dst_mac>
# eg. test.sh h1-eth0 10000 10.0.0.2 c6:e9:31:57:e3:c3
modprobe pktgen

function pgset() {
    local result

    echo $1 > $PGDEV

    result=`cat $PGDEV | fgrep "Result: OK:"`
    if [ "$result" = "" ]; then
         cat $PGDEV | fgrep Result:
    fi
}

function pg() {
    echo inject > $PGDEV
    cat $PGDEV
}

# Config Start Here -----------------------------------------------------------


# thread config
# Each CPU has own thread. Two CPU exammple. We add eth1, eth2 respectivly.

PGDEV=/proc/net/pktgen/kpktgend_0
  echo "Removing all devices"
# pgset "rem_device_all"
  echo "Adding interface"
 pgset "add_device $1"
#  echo "Setting max_before_softirq 10000"
# pgset "max_before_softirq 10000"

# device config
# delay 0 means maximum speed.

CLONE_SKB="clone_skb 1000"
# NIC adds 4 bytes CRC
PKT_SIZE="pkt_size 1000"

# COUNT 0 means forever
#COUNT="count 0"
COUNT="count $2"
DELAY="delay 1000"

PGDEV=/proc/net/pktgen/$1
  echo "Configuring $PGDEV"
 pgset "$COUNT"
 pgset "$CLONE_SKB"
 pgset "$PKT_SIZE"
 pgset "$DELAY"
 pgset "rate 1M"
 pgset "dst $3"
 pgset "dst_mac $4"

# Time to run
PGDEV=/proc/net/pktgen/pgctrl

 echo "Running... ctrl^C to stop"
 pgset "start"
 echo "Done"

# Result can be vieved in /proc/net/pktgen/eth[1,2]
