package org.jepria.oauth.main.security;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class CorsFilter implements Filter {
  /**
   * A preflight request is an OPTIONS request
   * with an Origin header.
   */
  private boolean isPreflightRequest(HttpServletRequest request) {
    return request.getHeader("Origin") != null
        && request.getMethod().equalsIgnoreCase("OPTIONS");
  }


  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

    // if there is no Origin header, then it is not a
    // cross origin request. We don't do anything.
    String origin = httpServletRequest.getHeader("Origin");
    String host = servletRequest.getScheme() + "://" + servletRequest.getServerName() + (servletRequest.getServerPort() != -1 ? ":" + servletRequest.getServerPort() : "");
    if (httpServletRequest.getHeader("Origin") == null || origin.equals(host)) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
    httpServletResponse.setHeader(
        "Access-Control-Expose-Headers",
        "Origin, Content-Type, Accept, Authorization, Extended-Response, X-Cache-Control, Cache-Control, Location");
    httpServletResponse.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("Origin"));

    if (isPreflightRequest(httpServletRequest)) {
      // Cross origin requests can be either simple requests
      // or preflight request. We need to add this header
      // to both type of requests. Only preflight requests
      // need specific headers.
      httpServletResponse.setStatus(200);
      httpServletResponse.setHeader("Access-Control-Allow-Methods",
          "GET, POST, PUT, DELETE, OPTIONS, HEAD");
      httpServletResponse.setHeader("Access-Control-Allow-Headers",
          // Whatever other non-standard/safe headers (see list above)
          // you want the client to be able to send to the server,
          // put it in this list. And remove the ones you don't want.
          "X-Requested-With, Authorization, Accept-Version, Content-MD5, CSRF-Token, Cache-Control, X-Cache-Control, Content-Type");
      return;
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {

  }
}