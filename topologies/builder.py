#!/usr/bin/python

from mininet.topo import Topo
from mininet.node import RemoteController
from mininet.log import setLogLevel

setLogLevel('info')

def add_controller(net):
    """Adds controller, listen on port 6633"""
    net.addController('c0', controller=RemoteController, ip='10.0.2.15', port=6633)

def create_simple(net):
    """Simple topology with 4 switches and 2 hosts"""

    # add four OpenFlow 1.3 nodes
    switches = []
    for i in range(4):
        switches.append(net.addSwitch('s'+str(i + 1), protocols="OpenFlow13"))

    # add two hosts
    hosts = []
    for i in range(2):
        hosts.append(net.addHost('h'+str(i + 1)))

    # create topology
    net.addLink(switches[0], hosts[0], bw=1, max_queue_size=1)
    net.addLink(switches[3], hosts[1], bw=1, max_queue_size=1)
    net.addLink(switches[0], switches[1], bw=1, max_queue_size=1)
    net.addLink(switches[0], switches[2], bw=1, max_queue_size=1)
    net.addLink(switches[3], switches[1], bw=1, max_queue_size=1)
    net.addLink(switches[3], switches[2], bw=1, max_queue_size=1)

    # start network
    net.start()

def create_kpnnl(net):
    """KPN Dutch Network, bandwidth and queue size are arbitrary"""

    switches = {}
    hosts = {}
    j = 1
    for i, p in enumerate(['Alkmaar', 'Amsterdam', 'Arnhem', 'Breda',
                           'DenBosch', 'DenHaag', 'Eindhoven', 'Enschede',
                           'Groningen', 'Heerlen', 'Hengelo', 'Leenwarden',
                           'Maastricht', 'Rotterdam', 'Roermond', 'Utrecht',
                           'Venlo', 'Zwolle']):
        switches[p] = net.addSwitch('s' + str(i + 1), protocols="OpenFlow13")

        # add hosts that will generate traffic in simulations
        if p in ['Amsterdam', 'Maastricht']:
            host = net.addHost('h' + str(j))
            j += 1
            net.addLink(switches[p], host)

    # add connections between switches
    net.addLink(switches['Alkmaar'], switches['Amsterdam'], bw=1, max_queue_size=1)
    net.addLink(switches['Alkmaar'], switches['DenHaag'], bw=1, max_queue_size=1)
    net.addLink(switches['Alkmaar'], switches['Leenwarden'], bw=1, max_queue_size=1)
    net.addLink(switches['Amsterdam'], switches['DenHaag'], bw=1, max_queue_size=1)
    net.addLink(switches['Amsterdam'], switches['Utrecht'], bw=1, max_queue_size=1)
    net.addLink(switches['Amsterdam'], switches['Zwolle'], bw=1, max_queue_size=1)
    net.addLink(switches['Arnhem'], switches['DenBosch'], bw=1, max_queue_size=1)
    net.addLink(switches['Arnhem'], switches['Enschede'], bw=1, max_queue_size=1)
    net.addLink(switches['Arnhem'], switches['Hengelo'], bw=1, max_queue_size=1)
    net.addLink(switches['Arnhem'], switches['Utrecht'], bw=1, max_queue_size=1)
    net.addLink(switches['Arnhem'], switches['Venlo'], bw=1, max_queue_size=1)
    net.addLink(switches['Arnhem'], switches['Zwolle'], bw=1, max_queue_size=1)
    net.addLink(switches['Breda'], switches['DenBosch'], bw=1, max_queue_size=1)
    net.addLink(switches['Breda'], switches['Eindhoven'], bw=1, max_queue_size=1)
    net.addLink(switches['Breda'], switches['Rotterdam'], bw=1, max_queue_size=1)
    net.addLink(switches['Breda'], switches['Utrecht'], bw=1, max_queue_size=1)
    net.addLink(switches['DenBosch'], switches['Eindhoven'], bw=1, max_queue_size=1)
    net.addLink(switches['DenBosch'], switches['Utrecht'], bw=1, max_queue_size=1)
    net.addLink(switches['DenBosch'], switches['Venlo'], bw=1, max_queue_size=1)
    net.addLink(switches['DenHaag'], switches['Rotterdam'], bw=1, max_queue_size=1)
    net.addLink(switches['DenHaag'], switches['Utrecht'], bw=1, max_queue_size=1)
    net.addLink(switches['Eindhoven'], switches['Roermond'], bw=1, max_queue_size=1)
    net.addLink(switches['Eindhoven'], switches['Venlo'], bw=1, max_queue_size=1)
    net.addLink(switches['Enschede'], switches['Hengelo'], bw=1, max_queue_size=1)
    net.addLink(switches['Groningen'], switches['Hengelo'], bw=1, max_queue_size=1)
    net.addLink(switches['Groningen'], switches['Leenwarden'], bw=1, max_queue_size=1)
    net.addLink(switches['Groningen'], switches['Zwolle'], bw=1, max_queue_size=1)
    net.addLink(switches['Heerlen'], switches['Roermond'], bw=1, max_queue_size=1)
    net.addLink(switches['Heerlen'], switches['Maastricht'], bw=1, max_queue_size=1)
    net.addLink(switches['Hengelo'], switches['Zwolle'], bw=1, max_queue_size=1)
    net.addLink(switches['Leenwarden'], switches['Zwolle'], bw=1, max_queue_size=1)
    net.addLink(switches['Maastricht'], switches['Roermond'], bw=1, max_queue_size=1)
    net.addLink(switches['Rotterdam'], switches['Utrecht'], bw=1, max_queue_size=1)
    net.addLink(switches['Roermond'], switches['Venlo'], bw=1, max_queue_size=1)
    net.addLink(switches['Utrecht'], switches['Zwolle'], bw=1, max_queue_size=1)
    net.start()

def create_sprint(net):
    """US Sprint network, bandwidth and queue size are arbitrary"""

    switches = {}
    hosts = {}
    for i, p in enumerate(['Anaheim', 'Atalanta', 'Boulder', 'Cheyenne',
                           'Chicago', 'Fort Worth', 'Kansas City', 'New York',
                           'Seattle', 'Stockton', 'Washington']):
        switches[p] = net.addSwitch('s' + str(i + 1), protocols="OpenFlow13")
        host = net.addHost('h' + str(i + 1))
        net.addLink(switches[p], host)

    # add connections between switches
    net.addLink(switches['Anaheim'], switches['Fort Worth'], bw=1, max_queue_size=10)
    net.addLink(switches['Anaheim'], switches['Stockton'], bw=1, max_queue_size=10)
    net.addLink(switches['Atalanta'], switches['Fort Worth'], bw=1, max_queue_size=10)
    net.addLink(switches['Atalanta'], switches['Washington'], bw=1, max_queue_size=10)
    net.addLink(switches['Boulder'], switches['Cheyenne'], bw=1, max_queue_size=10)
    net.addLink(switches['Cheyenne'], switches['Kansas City'], bw=1, max_queue_size=10)
    net.addLink(switches['Cheyenne'], switches['Stockton'], bw=1, max_queue_size=10)
    net.addLink(switches['Chicago'], switches['New York'], bw=1, max_queue_size=10)
    net.addLink(switches['Chicago'], switches['Seattle'], bw=1, max_queue_size=10)
    net.addLink(switches['Chicago'], switches['Stockton'], bw=1, max_queue_size=10)
    net.addLink(switches['Fort Worth'], switches['Kansas City'], bw=1, max_queue_size=10)
    net.addLink(switches['Fort Worth'], switches['Washington'], bw=1, max_queue_size=10)
    net.addLink(switches['Kansas City'], switches['Washington'], bw=1, max_queue_size=10)
    net.addLink(switches['New York'], switches['Stockton'], bw=1, max_queue_size=10)
    net.addLink(switches['New York'], switches['Washington'], bw=1, max_queue_size=10)
    net.addLink(switches['Seattle'], switches['Stockton'], bw=1, max_queue_size=10)
    net.addLink(switches['Washington'], switches['Stockton'], bw=1, max_queue_size=10)

    net.start()
