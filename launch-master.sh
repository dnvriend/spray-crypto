#!/bin/bash
HOSTNAME=192.168.0.27
NODE0=akka.tcp://ClusterSystem@192.168.0.27
NODE1=akka.tcp://ClusterSystem@192.168.0.6
./activator -Dakka.remote.netty.tcp.hostname=$HOSTNAME -Dakka.cluster.seed.nodes.0=$NODE0 -Dakka.cluster.seed.nodes.1=$NODE1 'run-main com.example.Main'
