# spray-crypto
Let's scale up! This time we will look at how fast we can make our REST service. Using routers, we can utilize 
every core to the max! 

Launch the REST server with:

    $ ./activator 'run-main com.example.Main'
    
Launch the REST client with:

    $ ./activator 'run-main com.example.Client'
    
or
    
    $ ./activator
    > runMain com.example.Client

Run the client a couple of times and then look at the debug output of the Client, scroll a bit up and look for the text:

I got the following values with 5 routers:

    No router:
    ==========
    Throughput for 1000 encrypts: 8044 ms
    
    With router:
    ============
    Throughput for 1000 encrypts: 1917 ms
    
And with 10 routers (guess the CPU was already utilized to the max)
    
    No router:
    ==========
    Throughput for 1000 encrypts: 7886 ms
    
    With router:
    ============
    Throughput for 1000 encrypts: 1593 ms

I got the values above on my good old MacBook@2009, it's only a Core Duo so I expect much more from modern processing
units!

Feel free to tweak the code and read [Akka Routing](http://doc.akka.io/docs/akka/snapshot/scala/routing.html)

Have fun!