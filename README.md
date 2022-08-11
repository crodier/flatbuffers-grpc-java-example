# Gradle + flatbuffers + grpc + spring boot

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

