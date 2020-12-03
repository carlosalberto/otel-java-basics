
otel_launcher_url := https://github.com/lightstep/otel-launcher-java/releases/download/0.11.0/lightstep-opentelemetry-javaagent-0.11.0.jar

build: lightstep-opentelemetry-javaagent-0.11.0.jar
	$(MAKE) -C server
	$(MAKE) -C client

lightstep-opentelemetry-javaagent-0.11.0.jar:
	curl -L -O ${otel_launcher_url}

run-server: build
	java -javaagent:lightstep-opentelemetry-javaagent-0.11.0.jar  \
	-Dls.service.name=hello-server \
	-Dotel.propagators=tracecontext,b3 \
	-Dotel.resource.attributes="something=else,container.name=my-container" \
	-Dotel.bsp.schedule.delay.millis=200 \
        -cp server/target/server-1.0-SNAPSHOT.jar \
        com.lightstep.examples.server.App

run-client: build
	java -javaagent:lightstep-opentelemetry-javaagent-0.11.0.jar  \
	-Dls.service.name=hello-client \
	-Dotel.propagators=tracecontext,b3 \
	-Dotel.bsp.schedule.delay.millis=200 \
        -cp client/target/client-1.0-SNAPSHOT.jar \
        com.lightstep.examples.client.App

clean:
	$(MAKE) clean -C server
	$(MAKE) clean -C client
