package org.jepria.oauth.main.rest.jersey.inject;

import org.glassfish.hk2.api.Factory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.Locale;

public class ClientLocaleFactory implements Factory<Locale> {
  
  @Context
  HttpServletRequest request;
  
  @Override
  public Locale provide() {
    if (request != null) {
      String localeParam = request.getParameter("locale");
      if (localeParam != null) {
        switch (localeParam) {
          case "ru": {
            return new Locale("ru", "RU");
          }
          case "en": {
            return new Locale("en", "EN");
          }
          default: return request.getLocale();
        }
      } else {
        return request.getLocale();
      }
    } else {
      return Locale.getDefault();
    }
  }
  
  @Override
  public void dispose(Locale locale) {
    locale = null;
  }
}
