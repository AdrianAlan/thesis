#!/usr/bin/python

from mininet.net import Mininet
#from mininet.node import OVSSwitch
from mininet.topo import Topo
from mininet.node import RemoteController
from mininet.log import setLogLevel
from mininet.cli import CLI

setLogLevel('info')

def empty():
    net = Mininet(controller=RemoteController)
    net.addController('c0', controller=RemoteController, ip='10.0.3.11', port=6633)

    switches = []
    for i in range(4):
        switches.append(net.addSwitch('s'+str(i + 1), protocols="OpenFlow13"))
    hosts = []
    for i in range(2):
        hosts.append(net.addHost('h'+str(i+1)))

    net.addLink(switches[0], hosts[0])
    net.addLink(switches[3], hosts[1])
    net.addLink(switches[0], switches[1])
    net.addLink(switches[0], switches[2])
    net.addLink(switches[3], switches[1])
    net.addLink(switches[3], switches[2])

    net.start()
    CLI(net)
    net.stop()

empty()
