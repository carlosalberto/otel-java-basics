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
  private static final Tracer tracer =
      OpenTelemetry.getTracer("com.lightstep.examples.client.App");

  public static void main( String[] args )
      throws Exception
    {
      Span span = tracer.spanBuilder("main").startSpan();
      
      try (Scope scope = tracer.withSpan(span)) {
        // create five requests. notice that the 
        // requests are linked together in the trace 
        // by the parent
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

  // annotations are an alternative to making spans by hand
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
