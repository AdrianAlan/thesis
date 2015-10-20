#!/usr/bin/python

from mininet.net import Mininet
from mininet.topo import Topo
from mininet.node import RemoteController
from mininet.log import setLogLevel
from mininet.cli import CLI

setLogLevel('info')
FIX_TIME = 1

def add_controller(net):
    net.addController('c0', controller=RemoteController, ip='10.0.3.11', port=6633)

def simple(net):
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

def kpnnl(net):
    switches = {}
    hosts = {}
    for i, p in enumerate(['Alkmaar', 'Amsterdam', 'Arnhem', 'Breda',
                           'DenBosch', 'DenHaag', 'Eindhoven', 'Enschede',
                           'Groningen', 'Heerlen', 'Hengelo', 'Leenwarden',
                           'Maastricht', 'Rotterdam', 'Roermond', 'Utrecht',
                           'Venlo', 'Zwolle']):
        switches[p] = net.addSwitch('s' + str(i), protocols="OpenFlow13")
        hosts[p] = net.addHost('h' + str(i))

    for i in ['Alkmaar', 'Amsterdam', 'Arnhem', 'Breda', 'DenBosch', 'DenHaag',
              'Eindhoven', 'Enschede', 'Groningen', 'Heerlen', 'Hengelo',
              'Leenwarden', 'Maastricht', 'Rotterdam', 'Roermond', 'Utrecht',
              'Venlo', 'Zwolle']:
        net.addLink(switches[i], hosts[i])

    # add connections between switches
    net.addLink(switches['Alkmaar'], switches['Amsterdam'])
    net.addLink(switches['Alkmaar'], switches['DenHaag'])
    net.addLink(switches['Alkmaar'], switches['Leenwarden'])
    net.addLink(switches['Amsterdam'], switches['DenHaag'])
    net.addLink(switches['Amsterdam'], switches['Utrecht'])
    net.addLink(switches['Amsterdam'], switches['Zwolle'])
    net.addLink(switches['Arnhem'], switches['DenBosch'])
    net.addLink(switches['Arnhem'], switches['Enschede'])
    net.addLink(switches['Arnhem'], switches['Hengelo'])
    net.addLink(switches['Arnhem'], switches['Utrecht'])
    net.addLink(switches['Arnhem'], switches['Venlo'])
    net.addLink(switches['Arnhem'], switches['Zwolle'])
    net.addLink(switches['Breda'], switches['DenBosch'])
    net.addLink(switches['Breda'], switches['Eindhoven'])
    net.addLink(switches['Breda'], switches['Rotterdam'])
    net.addLink(switches['Breda'], switches['Utrecht'])
    net.addLink(switches['DenBosch'], switches['Eindhoven'])
    net.addLink(switches['DenBosch'], switches['Utrecht'])
    net.addLink(switches['DenBosch'], switches['Venlo'])
    net.addLink(switches['DenHaag'], switches['Rotterdam'])
    net.addLink(switches['DenHaag'], switches['Utrecht'])
    net.addLink(switches['Eindhoven'], switches['Roermond'])
    net.addLink(switches['Eindhoven'], switches['Venlo'])
    net.addLink(switches['Enschede'], switches['Hengelo'])
    net.addLink(switches['Groningen'], switches['Hengelo'])
    net.addLink(switches['Groningen'], switches['Leenwarden'])
    net.addLink(switches['Groningen'], switches['Zwolle'])
    net.addLink(switches['Heerlen'], switches['Roermond'])
    net.addLink(switches['Heerlen'], switches['Maastricht'])
    net.addLink(switches['Hengelo'], switches['Zwolle'])
    net.addLink(switches['Leenwarden'], switches['Zwolle'])
    net.addLink(switches['Maastricht'], switches['Roermond'])
    net.addLink(switches['Rotterdam'], switches['Utrecht'])
    net.addLink(switches['Roermond'], switches['Venlo'])
    net.addLink(switches['Utrecht'], switches['Zwolle'])
    net.start()

def sprint(net):
    switches = {}
    hosts = {}
    for i, p in enumerate(['Anaheim', 'Atalanta', 'Boulder', 'Cheyenne',
                           'Chicago', 'Fort Worth', 'Kansas City', 'New York',
                           'Seattle', 'Stockton', 'Washington']):
        switches[p] = net.addSwitch('s' + str(i), protocols="OpenFlow13")
        hosts[p] = net.addHost('h' + str(i))

    for i in ['Anaheim', 'Atalanta', 'Boulder', 'Cheyenne', 'Chicago',
              'Fort Worth', 'Kansas City', 'New York', 'Seattle', 'Stockton',
              'Washington']:
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

def ping(net, h1, h2, i):
    return net.custom_ping([net.getNodeByName(h1), net.getNodeByName(h2)], interval=i, count=FIX_TIME/i, timeout=2)
