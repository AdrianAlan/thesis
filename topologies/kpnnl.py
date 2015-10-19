#!/usr/bin/python

from mininet.net import Mininet
#from mininet.node import OVSSwitch
from mininet.topo import Topo
from mininet.node import RemoteController
from mininet.log import setLogLevel
from mininet.cli import CLI

setLogLevel('info')

def kpn():
    net = Mininet(controller=RemoteController)
    net.addController('c0', controller=RemoteController, ip='10.0.3.11', port=6633)

    switches = {}
    hosts = {}
    for i, p in enumerate(['Alkmaar', 'Amsterdam', 'Arnhem', 'Breda',
                           'DenBosch', 'DenHaag', 'Eindhoven', 'Enschede', 'Groningen',
                           'Heerlen', 'Hengelo', 'Leenwarden', 'Maastricht',
                           'Rotterdam', 'Roermond', 'Utrecht', 'Venlo', 'Zwolle']):
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
    CLI(net)
    net.stop()

kpn()
