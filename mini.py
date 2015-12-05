from mininet.net import Mininet
from mininet.node import RemoteController
from topologies import builder
from mininet.cli import CLI
from mininet.node import CPULimitedHost
from mininet.link import TCLink

print "Creating network"
NET = Mininet(controller=None, host=CPULimitedHost, link=TCLink)
print "Adding Controller"
builder.add_controller(NET)
print "Starting topology"
#builder.create_simple(NET)
#builder.create_kpnnl(NET)
builder.create_sprint(NET)
CLI(NET)
NET.stop()
