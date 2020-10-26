package com.lightstep.examples.client;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.extensions.auto.annotations.WithSpan;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.Span;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class App
{
  public static void main( String[] args )
      throws Exception
    {
      Tracer tracer = OpenTelemetry.getTracerProvider().get("hello-client");
      Span span = tracer.spanBuilder("all requests").startSpan();
      try (Scope scope = tracer.withSpan(span)) {
        for (int i = 0; i < 5; i++) {
          makeRequest();
        }
      } finally {
        span.end();
      }

      // Allow the Spans to be flushed.
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
    }

  @WithSpan(value="make-request")
  static void makeRequest()
  {
    OkHttpClient client = new OkHttpClient();
    Request req = new Request.Builder()
      .url("http://localhost:9000/hello")
      .build();

    try (Response res = client.newCall(req).execute()) {
    } catch (Exception e) {
      System.out.println(String.format("Request failed: %s", e));
    }
  }
}
