package org.jepria.oauth.main.rest.jersey;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.jepria.oauth.authentication.rest.AuthenticationJaxrsAdapter;
import org.jepria.oauth.authorization.rest.AuthorizationJaxrsAdapter;
import org.jepria.oauth.client.rest.ClientJaxrsAdapter;
import org.jepria.oauth.clienturi.rest.ClientUriJaxrsAdapter;
import org.jepria.oauth.key.rest.KeyJaxrsAdapter;
import org.jepria.oauth.main.exception.OAuthExceptionMapper;
import org.jepria.oauth.main.rest.MainJaxrsAdapter;
import org.jepria.oauth.main.rest.jersey.inject.ClientLocaleFeature;
import org.jepria.oauth.main.rest.jersey.inject.ClientLocaleSupplier;
import org.jepria.oauth.session.rest.SessionJaxrsAdapter;
import org.jepria.oauth.token.rest.TokenJaxrsAdapter;
import org.jepria.server.service.rest.jersey.ApplicationConfigBase;
import org.jepria.server.service.security.HttpBasicDynamicFeature;
import org.jepria.server.service.security.JepOAuthDynamicFeature;

import java.util.Locale;

public class Application extends ApplicationConfigBase {
  
  public Application() {
    super();
    register(JepOAuthDynamicFeature.class);
    register(RequestLogFilter.class);
    register(MainJaxrsAdapter.class);
    register(AuthorizationJaxrsAdapter.class);
    register(SessionJaxrsAdapter.class);
    register(AuthenticationJaxrsAdapter.class);
    register(TokenJaxrsAdapter.class);
    register(ClientJaxrsAdapter.class);
    register(ClientUriJaxrsAdapter.class);
    register(KeyJaxrsAdapter.class);
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
  protected void registerHttpBasicDynamicFeature() {
  }

  @Override
  protected void registerExceptionMapperDefault() {
    register(OAuthExceptionMapper.class);
  }
}