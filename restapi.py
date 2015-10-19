"""REST API run script"""
from flask import Flask
from mininet.net import Mininet
from mininet.node import RemoteController
from subprocess import call
from topologies import simple
from topologies import kpnnl
from topologies import sprint

app = Flask(__name__)
VERSION = "0.0"
NET = Mininet(controller=RemoteController)

@app.route('/clean')
def clean():
    """Cleans the mess"""
    call(['sudo', 'mn', '-c'])
    return "Success"

@app.route('/link/<switch_a>/<switch_b>/<status>')
def link(switch_a, switch_b, status):
    """Changes link status"""
    NET.configLinkStatus(switch_a, switch_b, status)
    return "Success"

@app.route('/ping/<src_host>/<dest_host>')
def ping(src_host, dest_host):
    """Ping between hosts"""
    simple.ping(NET, str(src_host), str(dest_host))
    return "Success"

@app.route('/start/<topo>')
def start(topo):
    """Starts network"""
    if topo == "simple":
        simple.empty(NET)
    elif topo == "kpnnl":
        kpnnl.kpn(NET)
    elif topo == "sprint":
        sprint.sprint(NET)
    return "Success"

@app.route('/stop')
def stop():
    """Stops network and cleans"""
    NET.stop()
    return "Success"

@app.route('/version')
def version():
    """Returns version"""
    return ("Version %s\n" % VERSION)

if __name__ == '__main__':
    app.debug = True
    app.run(host='0.0.0.0', port=5555)
