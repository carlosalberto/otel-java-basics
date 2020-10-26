# OpenTelemetry Java Basics

Just a basic client/server for playing with OpenTelemetry in Java.

Define your Lightstep Acccess Token prior to run:

```sh
export LS_ACCESS_TOKEN=my-access-token-etc
```

Build:

```sh
make
```

Run the server and wait for it to be ready:

```sh
make run-server
```

Run the client to perform a few requests:

```sh
make run-client
```
