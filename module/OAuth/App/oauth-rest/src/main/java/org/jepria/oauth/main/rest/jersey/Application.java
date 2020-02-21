package org.jepria.oauth.main.rest.jersey;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.jepria.oauth.authentication.rest.AuthenticationJaxrsAdapter;
import org.jepria.oauth.authorization.rest.AuthorizationJaxrsAdapter;
import org.jepria.oauth.client.rest.ClientJaxrsAdapter;
import org.jepria.oauth.clienturi.rest.ClientUriJaxrsAdapter;
import org.jepria.oauth.main.exception.OAuthExceptionMapper;
import org.jepria.oauth.main.rest.jersey.inject.ClientLocaleFeature;
import org.jepria.oauth.main.rest.jersey.inject.ClientLocaleSupplier;
import org.jepria.oauth.main.security.AllowAllOriginFilter;
import org.jepria.oauth.main.security.ClientCredentialsRequestFilter;
import org.jepria.oauth.session.rest.SessionJaxrsAdapter;
import org.jepria.oauth.token.rest.TokenJaxrsAdapter;
import org.jepria.server.service.rest.jersey.ApplicationConfigBase;

import java.util.Locale;

public class Application extends ApplicationConfigBase {
  
  public Application() {
    super();
    register(AuthorizationJaxrsAdapter.class);
    register(SessionJaxrsAdapter.class);
    register(AuthenticationJaxrsAdapter.class);
    register(TokenJaxrsAdapter.class);
    register(ClientJaxrsAdapter.class);
    register(ClientUriJaxrsAdapter.class);
    register(ClientCredentialsRequestFilter.class);
    register(new ValidationExceptionMapper());
    register(ClientLocaleFeature.class);
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bindFactory(ClientLocaleSupplier.class)
          .to(Locale.class)
          .proxy(true)
          .proxyForSameScope(false)
          .in(RequestScoped.class);
      }
    });
  }

  @Override
  protected void registerValidation() {
  }

  /**
   * Отключен Allow-All CORS Handler
   */
  @Override
  protected void registerCorsHandler() {
    register(AllowAllOriginFilter.class);
  };

  @Override
  protected void registerExceptionMapperDefault() {
    register(OAuthExceptionMapper.class);
  }
}