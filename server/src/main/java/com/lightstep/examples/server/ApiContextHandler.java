package com.lightstep.examples.server;

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator.Getter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.StatusCanonicalCode;
import io.opentelemetry.trace.Tracer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ApiContextHandler extends ServletContextHandler
{
  public ApiContextHandler()
  {
    addServlet(new ServletHolder(new ApiServlet()), "/hello");
  }

  static final class ApiServlet extends HttpServlet
  {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
    {
      io.grpc.Context context = 
        OpenTelemetry.getPropagators().getTextMapPropagator().extract(
            io.grpc.Context.current(),
            req,
            HttpServletRequest::getHeader);

      Tracer tracer = OpenTelemetry.getTracerProvider().get("hello-server");
      Span span = tracer.spanBuilder("my-server-span").startSpan();
      try (Scope scope = tracer.withSpan(span)) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        span.setAttribute("ProjectId", "456");
        span.recordException(new RuntimeException("oops"));
        span.setStatus(StatusCanonicalCode.ERROR);
        span.addEvent("writing response",
            Attributes.of(AttributeKey.stringKey("http.route"), "hello world"));

        try (PrintWriter writer = res.getWriter()) {
          writer.write("Hello World");
        }
      } finally {
        span.end();
      }
    }
  }
}
