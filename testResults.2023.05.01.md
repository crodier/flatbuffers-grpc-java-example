### Most recent, with 1) Netty Epoll, native, and 2) Unsafe

#### Primary performance test machine (my desktop)
```shell
sudo lshw
```
```shell
 *-cpu
          description: CPU
          product: Intel(R) Core(TM) i7-4790K CPU @ 4.00GHz
          vendor: Intel Corp.
```

### Options
This enables Netty unsafe, which shows as enabled in Spring Boot.
```shell
--add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true
```

## Performance tests

### With PinnedThreadFactory (this commit)
Note:  3159 ms, lowest total millis for 20,000
```shell
Experiment size: 20000
--------------------- Blocking (much faster) --------------------
Took Nanos=3158576909
Avg Nanos=157928
Avg MICS=157
Took millis=3159, count=20000
Per milli=6, count=20000
Avg correlated mics=154.69846354999999
Min correlated mics=85
```

### With thread affinity (this commit)
Note:  First sub 160 RTT, First 84 mics best RTT
```shell
Experiment size: 20000
--------------------- Blocking (much faster) --------------------
Took Nanos=3250044674
Avg Nanos=162502
Avg MICS=162
Took millis=3251, count=20000
Per milli=6, count=20000
Avg correlated mics=158.95776665
Min correlated mics=84
```

### Warm-up the test before timing:  85 mics fastest RTT

```shell
Experiment size: 20000
--------------------- Blocking (much faster) --------------------
Took Nanos=3592959723
Avg Nanos=179647
Avg MICS=179
Took millis=3593, count=20000
Per milli=5, count=20000
Avg correlated mics=176.22431210000002
Min correlated mics=85
```

### 5-1-2023: Direct buffer for Netty Server builder.

```java
// Direct Executor makes sense, single threaded
nettyServerBuilder.directExecutor();
```

#### Results
```shell
Do request
--------------------- ASYNC (SLOW) --------------------
Time start is=603477950899571
Time start is=603477954966309
Took Nanos=569902522
Avg Nanos=56990
Avg MICS=56
Took millis=570, count=10000
Per milli=17, count=10000
Avg correlated mics=423086.8204344
Min correlated mics=317177
--------------------- Blocking (much faster) --------------------
Avg correlated mics=255.4281925
Min correlated mics=95
Took Nanos=2577476401
Avg Nanos=257747
Avg MICS=257
Took millis=2578, count=10000
Per milli=3, count=10000
Avg correlated mics=255.4281925
Min correlated mics=95
```

## Native netty EPoll and optimizations
#### Note the 96 mics round trip with native optimizations of Netty enabled
```shell
Do request
--------------------- ASYNC (SLOW) --------------------
Time start is=599881585434974
Time start is=599881590673273
Took Nanos=764797972
Avg Nanos=76479
Avg MICS=76
Took millis=765, count=10000
Per milli=13, count=10000
Avg correlated mics=561713.8192574
Min correlated mics=298884
--------------------- Blocking (much faster) --------------------
Avg correlated mics=292.2020981
Min correlated mics=96
Took Nanos=2955947193
Avg Nanos=295594
Avg MICS=295
Took millis=2956, count=10000
Per milli=3, count=10000
Avg correlated mics=292.2020981
Min correlated mics=96
```

### Baseline from non-native optimizations, Async and Blocking

```shell
Do request
--------------------- ASYNC (SLOW) --------------------
Time start is=590576141969679
Time start is=590576147185038
Time is=590576808494172
Took Nanos=677865191
Avg Nanos=67786
Avg MICS=67
Took millis=678, count=10000
Per milli=14, count=10000
Avg correlated mics=505870.2026173
Min correlated mics=287718
--------------------- Blocking (much faster) --------------------
Avg correlated mics=280.41398719999995
Min correlated mics=131
Took Nanos=2838105322
Avg Nanos=283810
Avg MICS=283
Took millis=2839, count=10000
Per milli=3, count=10000
Avg correlated mics=280.41398719999995
Min correlated mics=131
```

### 16 bytes - size the byte buffers
```shell
Do request
--------------------- ASYNC (SLOW) --------------------
Time start is=591205360875710
Time start is=591205366438454
Time is=591206123141821
Took Nanos=774331130
Avg Nanos=77433
Avg MICS=77
Took millis=775, count=10000
Per milli=12, count=10000
Avg correlated mics=567287.7477381
Min correlated mics=308461
--------------------- Blocking (much faster) --------------------
Avg correlated mics=285.82817239999997
Min correlated mics=110
Took Nanos=2892140401
Avg Nanos=289214
Avg MICS=289
Took millis=2893, count=10000
Per milli=3, count=10000
Avg correlated mics=285.82817239999997
Min correlated mics=110
```

### Size the byte buffer: 32 bytes
```shell
Do request
--------------------- ASYNC (SLOW) --------------------
Time start is=591404902491986
Time start is=591404907800667
Time is=591405827873307
Took Nanos=936734895
Avg Nanos=93673
Avg MICS=93
Took millis=937, count=10000
Per milli=10, count=10000
Avg correlated mics=636139.5798999
Min correlated mics=363552
--------------------- Blocking (much faster) --------------------
Avg correlated mics=274.1003606
Min correlated mics=108
Took Nanos=2768569903
Avg Nanos=276856
Avg MICS=276
Took millis=2769, count=10000
Per milli=3, count=10000
Avg correlated mics=274.1003606
Min correlated mics=108
```
