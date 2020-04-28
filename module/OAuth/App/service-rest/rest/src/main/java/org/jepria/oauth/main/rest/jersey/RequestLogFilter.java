package org.jepria.oauth.main.rest.jersey;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Date;

@Provider
@PreMatching
public class RequestLogFilter implements ContainerRequestFilter, ContainerResponseFilter {
  
  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    System.out.println("INCOMING REQUEST-" + new Date().toString());
    System.out.println(containerRequestContext.getMethod());
    System.out.println(containerRequestContext.getUriInfo().getRequestUri().toString());
  }
  
  @Override
  public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
    System.out.println("RESPONSE-" + new Date().toString());
    System.out.println("STATUS-" + containerResponseContext.getStatus());
  }
}
