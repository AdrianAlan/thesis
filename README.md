Failure Recovery and Routing Optimization in Software-Defined Networks
====

This respository contains the code for the fastfailover app which is a custom ONOS sevice. This code is a part of the M.Sc. thesis.

In order to fully use this repo, install all the dependencies in depend.info file. Service works for ONOS Drake, however it needs a bugfix.

After installation of ONOS 1.3 on your VM, you need to cheerypick commit 0be872498322e0e6c4aa2344b387ca053492ba33 and rebuild ONOS.

To install failover app, navigate to fastfailover-app\ and:
> mci

> onos-app $OC1 reinstall! target/fastfailover-app-1.3.0.oar

You now have active failover application in your ONOS server.
You can use mininet topologies form topologies\ to emulate network and run

>python scripts\bfdctl.py to activate per-link BFD session

In order to run rest api on top of mininet, run

>python restapi.py &

This will let you write tests in single bash file.

Thesis
---
Thesis was submitted at Technical University of Denmark, 2016
Author: Adrian Alan Pol
Supervisors: Jose Soler and Cosmin Marius Caba
