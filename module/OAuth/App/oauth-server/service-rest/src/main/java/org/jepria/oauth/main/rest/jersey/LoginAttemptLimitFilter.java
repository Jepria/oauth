package org.jepria.oauth.main.rest.jersey;

import org.jepria.server.env.EnvironmentPropertySupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Map;

@LoginAttempt
@Provider
public class LoginAttemptLimitFilter implements ContainerRequestFilter, ContainerResponseFilter {

  public static final Integer DEFAULT_MAX_ATTEMPT_COUNT = 3;
  public static final String OAUTH_LOGIN_MAX_ATTEMPT_COUNT = "OAUTH_LOGIN_MAX_ATTEMPT_COUNT";
  public static final String CURRENT_ATTEMPT_COUNT = "CURRENT_ATTEMPT_COUNT";
  @Context
  private HttpServletResponse httpServletResponse;

  public static Integer getMaxAttemptCount(HttpServletRequest httpServletRequest) {
    return Integer.valueOf(EnvironmentPropertySupport.getInstance(httpServletRequest).getProperty(OAUTH_LOGIN_MAX_ATTEMPT_COUNT, DEFAULT_MAX_ATTEMPT_COUNT.toString()));
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    Map<String, Cookie> cookieMap = containerRequestContext.getCookies();
    if (cookieMap.containsKey(CURRENT_ATTEMPT_COUNT)) {
      Integer currentAttemptCount = Integer.parseInt(cookieMap.get(CURRENT_ATTEMPT_COUNT).getValue()) + 1;
      containerRequestContext.setProperty(CURRENT_ATTEMPT_COUNT, currentAttemptCount);
    } else {
      containerRequestContext.setProperty(CURRENT_ATTEMPT_COUNT, 1);
    }
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
    Integer currentAttemptCount = (Integer) containerRequestContext.getProperty(CURRENT_ATTEMPT_COUNT);
    javax.servlet.http.Cookie cookie;
    if (currentAttemptCount == 0) {
      cookie = new javax.servlet.http.Cookie(CURRENT_ATTEMPT_COUNT, "");
      cookie.setHttpOnly(true);
      cookie.setMaxAge(-1);
    } else {
      cookie = new javax.servlet.http.Cookie(CURRENT_ATTEMPT_COUNT, currentAttemptCount.toString());
      cookie.setHttpOnly(true);
      cookie.setMaxAge(60*60*24);
    }
    httpServletResponse.addCookie(cookie);
  }
}
