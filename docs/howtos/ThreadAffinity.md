
# Thread Affinity
Thread affinity keeps the L1 and L2 data on one core, one NUMA chip.
```shell
sudo apt-get install libjna-java
```

https://github.com/peter-lawrey/Java-Thread-Affinity/wiki/Getting-started

## First, Set "isolcpus" (on Ubuntu)
Setting isolcpus isolates cores from being used.

Reasonable Ubuntu instructions from here:
https://askubuntu.com/questions/165075/how-to-get-isolcpus-kernel-parameter-working-with-precise-12-04-amd64

Edit **/etc/default/grub** and added **isolcpus=3,4** to GRUB_CMDLINE_LINUX_DEFAULT
```shell
sudo vi /etc/default/grub
# Edit this line with 'quiet splash'
GRUB_CMDLINE_LINUX_DEFAULT="quiet splash isolcpus=3,4"

sudo update-grub

# Reboot
sudo shutdown -r now
```

Check if it worked.  Last arg is number of cores
```shell
apt-get install stress && stress -c 8
```
Another window
```shell
top
```
### Then press '1'
.. and notice, cores 3 and 4 are not used.
```shell
Tasks: 283 total,  10 running, 209 sleeping,   0 stopped,   0 zombie
%Cpu0  : 99.3 us,  0.7 sy,  0.0 ni,  0.0 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
%Cpu1  : 96.0 us,  4.0 sy,  0.0 ni,  0.0 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
%Cpu2  : 99.7 us,  0.3 sy,  0.0 ni,  0.0 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
%Cpu3  :  0.0 us,  0.0 sy,  0.0 ni,100.0 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
%Cpu4  :  0.0 us,  0.7 sy,  0.0 ni, 99.3 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
%Cpu5  : 98.7 us,  1.3 sy,  0.0 ni,  0.0 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
%Cpu6  :100.0 us,  0.0 sy,  0.0 ni,  0.0 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
%Cpu7  : 98.0 us,  1.7 sy,  0.0 ni,  0.0 id,  0.0 wa,  0.0 hi,  0.3 si,  0.0 st
KiB Mem : 32773032 total, 22878868 free,  4397596 used,  5496568 buff/cache
KiB Swap:  2097148 total,  2097148 free,        0 used. 27747792 avail Mem 
```

### Disable IRQBALANCE for these cores
https://serverfault.com/questions/380935/how-to-ban-hardware-interrupts-with-irqbalance-banned-cpus-on-ubuntu

```shell
IRQBALANCE_BANNED_CPUS="3f"
```
Restart 'irqbalance'
```shell
sudo service irqbalance stop
sudo service irqbalance start
```

## Install Java-Thread-Affinity

### Add to gradle
```shell
// https://mvnrepository.com/artifact/net.openhft/affinity
implementation 'net.openhft:affinity:3.23.3'
```

### Netty instructions for Thread Affinity, use this in gRPC
https://netty.io/wiki/thread-affinity.html

```java
final int acceptorThreads = 1;
final int workerThreads = 10;
EventLoopGroup acceptorGroup = new NioEventLoopGroup(acceptorThreads);
ThreadFactory threadFactory = new AffinityThreadFactory("atf_wrk", AffinityStrategies.DIFFERENT_CORE);
EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads, threadFactory);
```