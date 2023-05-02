1.  Set thread affinity:  https://github.com/OpenHFT/Java-Thread-Affinity
   2. find and finish - // TODO:  Optimize such that these are to cores on one socket, and re-test 
3. Need a performance test using HA Proxy, and perhaps against protos and C++
   What is the latency, ideally
3.  Then, need to optimize it, see how fast it can go
4.  Need real schema for New Order, Cancel, Fill (review FIX guide)
4.  Need a FIX Engine, probably Chronicle, looks good:  https://chronicle.software/fix-engine/
2.  Need a performance unit test.