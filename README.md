# Gradle + flatbuffers + grpc + spring boot

## Notes

1.  Open JDK 17:  
    ```shell
            sudo apt install openjdk-17-jdk
            sudo update-alternatives --config java
    ```
    The java alternative needs to be Java 17 for Spring boot.  This can also
    be done using gradle properties in your home folder .gradle.
1.  Reasonable gradle install for Mint
    https://www.markaicode.com/gradle-installation-on-linux-mint-21/

1.  IntelliJ, change Gradle to be Java 17 (if need be)
https://stackoverflow.com/questions/72117858/incompatible-because-this-component-declares-a-component-compatible-with-java-11

2.  The flatbuffers compiler needs to be a binary.
"flatc" in "fb-lib/flatc", needs to be binary, and 23.3.3.
If it is not, build flatc 23.3.3 and put it there.
    
## Build
```shell
./gradlew build
```
## Run
To run gRPC server
```shell
./gradlew :grpc-server:bootRun
```
To run a test-app client
```shell
./gradlew :grpc-test-app:run
```       
## Under hood
Bare minimal set of libraries, plugins and code to run gRPC server. Only official libraries and plugins are used.
### fb-lib
Module with library flavour holds `schema.fbs`, generates java sources and provides transitive dependencies for flatbuffers and grpc
### grpc-server
Module with spring boot app, basically spring controls grpc server lifecycle and provides properties
### grpc-client
Module with library flavour adds `stubs` over `fb-lib` and add UX to generated flatbuffers client
### grpc-test-app
Module with application flavour, onliner of `grpc-client` usage

