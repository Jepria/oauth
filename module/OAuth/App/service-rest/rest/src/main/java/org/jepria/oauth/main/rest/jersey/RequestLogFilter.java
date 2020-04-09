package org.jepria.oauth.main.rest.jersey;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;
import java.util.Date;

public class RequestLogFilter implements ContainerRequestFilter {
  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    System.out.println("INCOMING REQUEST-" + new Date().toString());
    System.out.println(containerRequestContext.getMethod());
    System.out.println(containerRequestContext.getUriInfo().getRequestUri().toString());
  }
}
