"""REST API run script"""
from flask import Flask
from mininet.net import Mininet
from mininet.node import RemoteController
from subprocess import call
from topologies import builder

app = Flask(__name__)
VERSION = "0.0"
NET = Mininet(controller=RemoteController)

@app.route('/clean')
def clean():
    """Cleans the mess"""
    call(['sudo', 'mn', '-c'])
    return "Success \n"

@app.route('/link/<switch_a>/<switch_b>/<status>')
def link(switch_a, switch_b, status):
    """Changes link status"""
    NET.configLinkStatus(switch_a, switch_b, status)
    return "Success \n"

@app.route('/ping/<src_host>/<dest_host>/<interval>')
def ping(src_host, dest_host, interval):
    """Ping between hosts"""
    return "Dropped: %s \n" % builder.ping(NET, str(src_host), str(dest_host), float(interval))

@app.route('/start/<topo>')
def start(topo):
    """Starts network"""
    builder.add_controller(NET)
    if topo == "simple":
        builder.simple(NET)
    elif topo == "kpnnl":
        builder.kpnnl(NET)
    elif topo == "sprint":
        builder.sprint(NET)
    else:
        return "Nerwork not found"
    return "Success \n"

@app.route('/stop')
def stop():
    """Stops network and cleans"""
    NET.stop()
    return "Success \n"

@app.route('/version')
def version():
    """Returns version"""
    return ("Version %s \n" % VERSION)

if __name__ == '__main__':
    app.debug = True
    app.run(host='0.0.0.0', port=5555)
