# JunkTransfer

## Dependencies
This project requires JDK 1.8 and Ant build system. 

## Build Instruction
$ cd JunkTransfer  
$ ant clean all  

## Run Instruction
$ cd JunkTransfer
$ java -classpath "lib/*:out/production/JunkTransfer"  
net.amirrazmjou.Main -s data/test_large 10240 192.168.126.1:6666   
192.168.126.1:6667 192.168.126.1:6668 192.168.126.1:6669  

## How you tested your solution.
I created several loopback interfaces on my local desktop, I also assigned different IP address and networks to each one of the loopback interfaces. Later I used Wireshark, tcpdump, iftop to monitor the bandwidth usage of the server. I also interrupted some of clients during the test on purpose to make sure that the server can adjust the extra free bandwidth.
One of the advantage of loopback interfaces is that it makes it possible to test distributed applications like this project on a single machine.  
I also used vagrantup to create a configurable, test environment of virtual machines for nodes and clients 

## Description
I used leaky bucket algorithm to limit the server bandwidth. As our server application has multiple network connection to clients simultaneously. I also assumed that our clients may consume the bandwidth unevenly and there is high chance of having some clients to finish downloading their part before the other clients. The server should honor exploit the rate set by the user at the same time.
The leaky bucket algorithm implemented as a shared object accessible among server threads is a good choice. As clients consume server bandwidth the tokens inside the leaky bucket is decreased. If there are not enough tokens in the bucket the client would be blocked.  The bucket is refilled on a constant time periods and number of tokens. It automatically adjusts the bandwidth in case of interruption or congestions in clients as the leaky bucket algorithm does not discriminate between the consumers (each thread consumes one token upon the uploading one byte of data).  
I also implemented a new OutputStream class. The constructor for this new class takes a bucket object so the OutStream instances can share a single bucket object so regardless of number of OutputStream object instantiated the aggregated traffic bandwidth cannot exceed what specified in the bucket object. TokenBucket class also uses very low granular timer to not yield the caller more than expected; this allows for a more bandwidth distribution among flows as well as more responsiveness to client usage.
I also made the TokenBucket thread-safe as the bucket object is going to be shared among several threads.  The critical code block in the TokenBucket is the part that bucket is refilled and next refill time is updated.   
I also employed “Philip Isenhour” CompressedBlockOutputStream, CompressedBlockOutputStream to compress the data as it being sent over the wire. The alternative solutions would be to compress the entire chunk or the original file before it is sent over the wire. The problem with other typical techniques of compression as it is mentioned in his web site is that large compression operation takes a considerable amount of CPU resource before we start to transmit the data whereas making the transmission data into even smaller chunks (1024 bytes in our solution) makes a good utilization of CPU and network resources and gaining a good compression ratio over the time of transmission. The compression chunk size is still a trade-off between computation overhead (on both client and servers) and network resources. 

### Performance
The use of low granular timer, fine tuned token bucket allows the best utilization of minimum bandwidth specified by the user.  

Applying ZLIB compression to the traffic flow may result in much less communication overhead depending on the entropy of the data.  

I used Buffers Streams on all IO streams with a single exception for socket output stream as it is required to have full control on the number of bytes being sent in order to limit the bandwidth.  Guava ByteStreams.copy simply uses block-based copy, the copy operation is as fast as it can.  
The constructor of SpeedLimitedOutputStream takes a burst value to specify the maximum number of bytes that can be sent over the wire instantly on a single SpeedLimitedOutputStream.write(…) call. This provides the ability to make a choice between CPU resources vs burstiness of the traffic on the wire.   


