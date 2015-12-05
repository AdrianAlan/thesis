#!/usr/bin/env python
import subprocess
import simplejson as json
import sys

if len(sys.argv) > 1:
    f = open(sys.argv[1])
    links = f.read()
    interfaces = {}
    proc1 = subprocess.Popen(("ovs-vsctl", "list", "interface"), stdout=subprocess.PIPE)
    proc2 = subprocess.check_output(('grep', '_uuid'), stdin=proc1.stdout)
    proc1.wait()
    for p in proc2.split("\n"):
        if len(p):
            interface = p.split(":")[1][1:]
            pro0 = subprocess.Popen(("ovs-vsctl", "list", "interface", interface), stdout=subprocess.PIPE)
            pro1 = subprocess.check_output(('grep', ''), stdin=pro0.stdout)
            for p in pro1.split("\n"):
                if 'name ' in p:
                    name = p.split('"')[1]
                elif 'mac_in_use ' in p:
                    mac = p.split('"')[1]
            interfaces[name] = dict()
            interfaces[name]["_uuid"] = interface
            interfaces[name]["mac"] = mac

    links = json.loads(links)
    for link in links:
        subprocess.call(["ovs-vsctl", "set", "interface", interfaces[link["from"]]["_uuid"],
                         "bfd:enable=true,bfd:min_rx=1,bfd:min_tx:1,bfd:bfd_local_dst:" + interfaces[link["to"]]["mac"]])
