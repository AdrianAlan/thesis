#!/usr/bin/python

from mininet.net import Mininet
from mininet.topo import Topo
from mininet.node import RemoteController
from mininet.log import setLogLevel
from mininet.cli import CLI

def sprint(net):
    net.addController('c0', controller=RemoteController, ip='10.0.3.11', port=6633)

    switches = {}
    hosts = {}
    for i, p in enumerate(['Anaheim', 'Atalanta', 'Boulder', 'Cheyenne',
                           'Chicago', 'Fort Worth', 'Kansas City', 'New York',
                           'Seattle', 'Stockton', 'Washington']):
        switches[p] = net.addSwitch('s' + str(i), protocols="OpenFlow13")
        hosts[p] = net.addHost('h' + str(i))

    for i in ['Anaheim', 'Atalanta', 'Boulder', 'Cheyenne', 'Chicago',
              'Fort Worth', 'Kansas City', 'New York', 'Seattle', 'Stockton',
              'Washington']):
        net.addLink(switches[i], hosts[i])

    # add connections between switches
    net.addLink(switches['Anaheim'], switches['Fort Worth'])
    net.addLink(switches['Anaheim'], switches['Stockton'])
    net.addLink(switches['Atalanta'], switches['Fort Worth'])
    net.addLink(switches['Atalanta'], switches['Washington'])
    net.addLink(switches['Boulder'], switches['Cheyenne'])
    net.addLink(switches['Cheyenne'], switches['Kansas City'])
    net.addLink(switches['Cheyenne'], switches['Stockton'])
    net.addLink(switches['Chicago'], switches['New York'])
    net.addLink(switches['Chicago'], switches['Seattle'])
    net.addLink(switches['Chicago'], switches['Stockton'])
    net.addLink(switches['Fort Worth'], switches['Kansas City'])
    net.addLink(switches['Fort Worth'], switches['Washington'])
    net.addLink(switches['Kansas City'], switches['Washington'])
    net.addLink(switches['New York'], switches['Stockton'])
    net.addLink(switches['New York'], switches['Washington'])
    net.addLink(switches['Seattle'], switches['Stockton'])
    net.addLink(switches['Washington'], switches['Stockton'])

    net.start()
