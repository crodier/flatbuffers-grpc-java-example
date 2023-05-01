### Netty Optimization notes

To build the .so file for epoll netty
1.  Clone netty
2.  Checkout a particular branch, perhaps 4.1.77 which is the version of Netty grpc is using
3. But can also build from the head of 4.1 which is 4.1.93, not yet released
4. Or build 4.1.92 most recent
1. cd to the 'transport-native-epoll' folder, and run 'package
6. Link the jni.h folder in java (`locate` it first) into ./include from this folder in Netty
6. run 'compile' target to generate the SO file
6. Copy the SO file to this project from `target/native-build/target/lib`

### Notes on getting Netty Native EPOLL working:
1. This shaves about 10% off the blocking grpc calls, from 110 usecs to 96 usecs 
2. To work around a problem with a method not found when loading the library,
    I put `implementation 'io.netty:netty-transport-native-unix-common:4.1.92.Final` at the front of the build.gradle classpath
2.  To work around failure of Spring Boot / grpc / Netty to find and load
    the native EPOLL shared object (so) file, I built the shared object file
   `libnetty_transport_native_epoll_x86_64.so` on my box, with OpenJRE 17, Linux Mint
    and then committed this as a binary.
3.  Next, in the grpc server, I use System.load(), with the path to this file,
    which assumes it is in the root folder with the binary
    (this is important for deployment, it must be copied out to that location)