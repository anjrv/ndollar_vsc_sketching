# Sketching server

### Description

This is a java gRPC server set up using maven.

The intention is to migrate a subset of parts we need from the sketching portal to here. Mostly that involves the $N parsing code.

The proto definitions are visible under `src/main/proto`. For now it should be sufficient to stick to that one file and use minimal imports.

The server itself and the method overrides for the proto rpc calls are under `src/main/java/is/nsn/sketching` ( Everyone loves Java package paths ... )

### Usage

In order to be able to build and run this a recent `protoc` needs to be on your `PATH`. Most operating systems have native packages for this but there are also pre-compiled binaries that can be used instead. More information here: [Compiler Installation](https://grpc.io/docs/protoc-installation/)

Once that is done installation, indexing, and building should work as normal. It may be necessary to right click run the server the first time.

If there is a preference for running this on the command line then `maven` has to be installed as well. Most IDEs provide this.

These can be handled by an IDE like IntelliJ. Alternatively you can run the entire thing on the command line using the following commands:

```
mvn clean install
mvn compile
mvn exec:java
```
