#!/usr/bin/python

#from mininet.net import Mininet
#from mininet.node import OVSSwitch
from mininet.topo import Topo
from mininet.log import setLogLevel
#from mininet.cli import CLI

setLogLevel('info')

class Simplest(Topo):

    def __init__(self, **opts):
        Topo.__init__(self, **opts)
        
        switches = []
        for i in range(4):
            switches.append(self.addSwitch('s'+str(i + 1), protocols="OpenFlow13"))
        hosts = []
        for i in range(2):
            hosts.append(self.addHost('h'+str(i+1)))

        self.addLink(switches[0], hosts[0])
        self.addLink(switches[3], hosts[1])
        self.addLink(switches[0], switches[1])
        self.addLink(switches[0], switches[2])
        self.addLink(switches[3], switches[1])
        self.addLink(switches[3], switches[2])

topos = {
    'T0': Simplest
}
