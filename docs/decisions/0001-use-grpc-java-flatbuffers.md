# Use Flatbuffers and gRPC w/ Java

## Context and Problem Statement

Messaging is critical for most applications,
in particular trading.  There are two 
key situations when you want very fast and
efficient messaging, which are also CPU friendly.

1.  Low Latency / High Frequency Trading
2.  Big-data / Advertising / analytics cases

The overall systems should be generally consistent
where possible, to make it developer friendly.

The messages need a schema to make the
messages correct between the system,
and correctness known at compile time
is a major advantage of schematized messages.

Overall major factors:
1.  Speed
1.  Zero-copy vs. one-copy
1.  Schematized vs. JSON / no-schema
1.  Polyglot support
1.  Overall industry support and usage

Generally it is well understood that **zero-copy**
messages are far faster and more CPU friendly,
which in big data environments, can save 
millions, or tens of millions of dollars in cost
while sending messages quicker.

If not for zero-copy, protobuf and gRPC 
are widely considered leaders in the remote
communication space today.  gRPC provides
clean interfaces, mature and fast code,
polyglot support, strong documentation, and
is backed by Google and an active community.
Without zero-copy as a concern, gRPC w/ Protos
is the defacto standard for most modern
software architectures.  The why goes
beyond the scope of this decision, but 
this is generally accepted as of April 2023.

Flatbuffers has been invented at Google
and is compatible with gRPC with minor 
usability disadvantages, but is zero-copy.

This repository proves this is a viable 
approach which can be built quickly
and used in Java w/ Spring Boot 3.0 
(the primary support is C++.)

## Considered Options

* [gRPC Flatbuffers](https://grpc.io/blog/grpc-flatbuffers/) 
* [Aeron](https://aeron.io/) from Martin Fowler and team
  * **High cost to use:** Aeron is extremely expensive to code in and use, and could easily double or triple the timeline of any effort,
    as the primary drawback.
  * **Training** It is hard to find resources familiar with Aeron, and it is not easy to learn and use.
  * Aeron is the winner if you need *1 microsecond (u*sec), e.g. true High Frequency **MARKET MAKING** (e.g. sell side)
  * Aeron uses reliable UDP
  * Aeron is *not* widely adopted.
* [gRPC w/ protobuf](https://grpc.io/) Protobuf is the default messaging for gRPC
* [CapN Proto](https://capnproto.org/)
  * CapN Proto is a real contendor, selected by a trading team at a major hedge fund.
  * At time of writing, there is still no Java RPC listed on the other langages
  page for CapNProto:  https://capnproto.org/otherlang.html
  * Support for Java is shaky
  * From the Flatbuffers wiki:  "Cap'n'Proto promises to reduce Protocol Buffers much like FlatBuffers does, though with a more complicated binary encoding and less flexibility (no optional fields to allow deprecating fields or serializing with missing fields for which defaults exist)"
* [C++ Cereal](https://uscilab.github.io/cereal/)
    * Cereal is only for C++, not Polyglot
    * Lack of general adoption in industry / support of a major technology shop
* Thrift (from facebook)
    * Thrift support is not as good as gRPC
    * Lack of RPC client server support
* Avro (Hadoop messaging format)
    * Lacks rpc support (client server code generation)

## Decision Outcome

Chosen option: "gRPC w/ Flatbuffers", because

1.  Theory
      * zero copy is clearly more CPU friendly
      * less work for the CPU means more speed, less compute
1. Speed + Benchmarks:
   
    * benchmarks page of Flatbuffers:  https://flatbuffers.dev/md__benchmarks.html

1. Google support of Flatbuffers   

    * Google gaming division supports flatbuffers

1.  Relative ease of use, low cost

    * Flatbuffers and gRPC can be spun up in half a day

## More Information

#### Flatbuffers own explanation:
https://github.com/google/flatbuffers/wiki/Why-FlatBuffers-vs-other-options%3F

### Usage of Flatbuffers (broad, and for important high performance systems)

https://mvnrepository.com/artifact/com.google.flatbuffers/flatbuffers-java/usages

#### Serialization library usage (17th at time of writing)

https://mvnrepository.com/open-source/object-serialization?p=2

## Future Research

Netty usage in Espresso, with EPoll
https://engineering.linkedin.com/blog/2019/06/espresso-new-netty-framework

70% improvement for Netty w/ SolarFlare (kernell bypass)
https://www.xilinx.com/publications/results/onload-netty-io-benchmark-results.pdf

EFA (Elastic Fabric Adapter) for Kernel Bypass networking on AWS
https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/efa.html

#### Netty EPoll is challenging to add, but adding it..
https://netty.io/wiki/native-transports.html

#### More Netty optimizations
https://stackoverflow.com/questions/57885828/netty-cannot-access-class-jdk-internal-misc-unsafe

#### Benchmarks

https://flatbuffers.dev/flatbuffers_benchmarks.html
https://github.com/LesnyRumcajs/grpc_bench
