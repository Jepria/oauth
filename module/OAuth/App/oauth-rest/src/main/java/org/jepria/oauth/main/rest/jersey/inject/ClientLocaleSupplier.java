package org.jepria.oauth.main.rest.jersey.inject;

import org.glassfish.jersey.server.ContainerRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientLocaleSupplier implements Function<ContainerRequest, Locale>, Supplier<Locale> {

  @Context
  HttpServletRequest request;

  private Locale getLocale() {
    if (request != null) {
      return request.getLocale();
    } else {
      return Locale.getDefault();
    }
  }

  @Override
  public Locale apply(ContainerRequest containerRequest) {
    return getLocale();
  }

  @Override
  public Locale get() {
    return getLocale();
  }
}
