@echo off
SET HOSTNAME=192.168.0.6
SET PORT=2554
SET NODE0=akka.tcp://ClusterSystem@192.168.0.6
SET NODE1=akka.tcp://ClusterSystem@192.168.0.27
java -jar activator-launch-1.2.2.jar -Dakka.remote.netty.tcp.hostname=%HOSTNAME% -Dakka.remote.netty.tcp.port=%PORT% -Dakka.cluster.seed.nodes.0=%NODE0% -Dakka.cluster.seed.nodes.1=%NODE1% "run-main com.example.Main"