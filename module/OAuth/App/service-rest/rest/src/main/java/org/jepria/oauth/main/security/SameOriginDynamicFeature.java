package org.jepria.oauth.main.security;

import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.ws.rs.container.*;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;


@Provider
@PreMatching
public class SameOriginDynamicFeature implements DynamicFeature {
  
  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
    // HttpBasic annotation on the method
    CrossOrigin resourceAnnotation = resourceInfo.getResourceClass().getAnnotation(CrossOrigin.class);
    CrossOrigin methodAnnotation = am.getAnnotation(CrossOrigin.class);
//    if (resourceAnnotation != null) {
//      context.register(new CorsFilter());
//      return;
//    } else if (methodAnnotation != null) {
//      context.register(new CorsFilter());
//      return;
//    }
  }
  
  @Provider
  @PreMatching
  public static class SameOriginFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    /**
     * Method for ContainerRequestFilter.
     */
    @Override
    public void filter(ContainerRequestContext request) throws IOException {
      
      // If it's a preflight request, we abort the request with
      // a 200 status, and the CORS headers are added in the
      // response filter method below.
      if (isPreflightRequest(request)) {
        request.abortWith(Response.ok().build());
        return;
      }
    }
    
    /**
     * A preflight request is an OPTIONS request
     * with an Origin header.
     */
    private boolean isPreflightRequest(ContainerRequestContext request) {
      return request.getHeaderString("Origin") != null
          && request.getMethod().equalsIgnoreCase("OPTIONS");
    }
    
    /**
     * Method for ContainerResponseFilter.
     */
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response)
        throws IOException {
      
      // if there is no Origin header, then it is not a
      // cross origin request. We don't do anything.
      if (request.getHeaderString("Origin") == null) {
        return;
      }
      
      // If it is a preflight request, then we add all
      // the CORS headers here.
      if (isPreflightRequest(request)) {
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.getHeaders().add("Access-Control-Allow-Headers",
            // Whatever other non-standard/safe headers (see list above)
            // you want the client to be able to send to the server,
            // put it in this list. And remove the ones you don't want.
            "X-Requested-With, Authorization, " +
                "Accept-Version, Content-MD5, CSRF-Token, Content-Type");
      }
      
      // Cross origin requests can be either simple requests
      // or preflight request. We need to add this header
      // to both type of requests. Only preflight requests
      // need the previously added headers.
      response.getHeaders().add("Access-Control-Allow-Origin", "*");
    }
  }
}
