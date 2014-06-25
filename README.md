# spray-crypto
Let's scale out! This time we will look at how reliable we can distribute our workload using a cluster configuration.
Note, this solution isn't about speed, its about distribution of work. 

# Cluster configuration
* MacBook@2009 as master @ 192.168.0.6 / Gb Ethernet

    $ ./launch-master.sh
    
We will first start the master, there is nothing special about the master node, only that it's the first node
that's running, and therefor the oldest one.

Next we will launch nodes one by one that will become a member of the cluster and run the client each time and measure
the performance.
    
* Asus Core i7/16GB/250GB SSD as slave @ 192.168.0.27 / Gb Ethernet

    c:\project\spray-crypto>launch-node.bat
    c:\project\spray-crypto>launch-node2.bat
    c:\project\spray-crypto>launch-node3.bat

# Launch the REST client with

    $ ./launch-client.sh

# Performance with local routees

## Only local

    No router:
    ==========
    Throughput for 1000 encrypts: 6652 ms
    
    With router:
    ============
    Throughput for 1000 encrypts: 2270 ms
    
    With cluster:
    ============
    Throughput for 1000 encrypts: 4736 ms

## 1 node

    No router:
    ==========
    Throughput for 1000 encrypts: 4306 ms
    
    With router:
    ============
    Throughput for 1000 encrypts: 1326 ms
    
    With cluster:
    ============
    Throughput for 1000 encrypts: 3714 ms

## 2 nodes
    No router:
    ==========
    Throughput for 1000 encrypts: 5161 ms
    
    With router:
    ============
    Throughput for 1000 encrypts: 1188 ms
    
    With cluster:
    ============
    Throughput for 1000 encrypts: 3934 ms

## 3 nodes

    No router:
    ==========
    Throughput for 1000 encrypts: 4741 ms
    
    With router:
    ============
    Throughput for 1000 encrypts: 1354 ms
    
    With cluster:
    ============
    Throughput for 1000 encrypts: 4434 ms
    
# Performance without local routees
    
# 1 node
    
    No router:
    ==========
    Throughput for 1000 encrypts: 6083 ms
    
    With router:
    ============
    Throughput for 1000 encrypts: 1847 ms
    
    With cluster:
    ============
    Throughput for 1000 encrypts: 5409 ms

# 2 nodes

    No router:
    ==========
    Throughput for 1000 encrypts: 4808 ms
    
    With router:
    ============
    Throughput for 1000 encrypts: 1153 ms
    
    With cluster:
    ============
    Throughput for 1000 encrypts: 4940 ms

# 3 nodes

    No router:
    ==========
    Throughput for 1000 encrypts: 4807 ms
    
    With router:
    ============
    Throughput for 1000 encrypts: 1035 ms
    
    With cluster:
    ============
    Throughput for 1000 encrypts: 4676 ms

# Performance conclusion
There is a penalty to be paid when using a cluster:

* Most importantly, and not handled in this example; no reliable messaging, so it's fire and forget, best effort at best!
* Clustering using standard networking technologies like me at home, costs performance! However, when used correctly, 
a cluster solution can be used for high availability. Hybrid solutions can be created to gain best of both worlds, 
but as with a RAID array, getting high availability goes against performance. 

I'm going to fiddle around a little more..

Have fun!