package com.lightstep.examples.server;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ApiContextHandler extends ServletContextHandler
{
  // name your tracer after the class it instruments
  // spans started with this tracer will then 
  // be attributed to this package
  private static final Tracer tracer =
      OpenTelemetry.getGlobalTracer("com.lightstep.examples.server.ApiContextHandler");

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
      // the current span has automatically been created by 
      // the servlet instrumentation
      Span span = Span.current();
      
      // define the route name using semantic conventions
      // https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/semantic_conventions/http.md#http-server-semantic-conventions
      span.setAttribute("http.route", "hello");

      // pretend to do work
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }

      // start a child span
      Span childSpan = tracer.spanBuilder("my-server-span").startSpan();
      try (Scope scope = childSpan.makeCurrent()) {
        
        // inside the new scope, getCurrentSpan returns childSpan.
        // note that span methods can be chained.
        Span.current().setAttribute("ProjectId", "456");
          

        // recordException automatically formats an exception event
        childSpan.recordException(new RuntimeException("oops"));
        
        // in order for an exception to counts as an error,
        // the status on the span must be to Error
        childSpan.setStatus(StatusCode.ERROR);
        
        // pretend to do work
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        
        try (PrintWriter writer = res.getWriter()) {
          
          // events are structured logs, contextualized by the trace.
          childSpan.addEvent("writing response",
          Attributes.of(AttributeKey.stringKey("content"), "hello world"));
          
          writer.write("Hello World");
        }

      } finally {
        // make sure to close the span
        childSpan.end();
      }
    }
  }
}
