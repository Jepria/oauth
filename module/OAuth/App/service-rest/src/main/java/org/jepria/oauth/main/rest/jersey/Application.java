package org.jepria.oauth.main.rest.jersey;

import org.jepria.oauth.authentication.rest.AuthenticationJaxrsAdapter;
import org.jepria.oauth.authorization.rest.AuthorizationJaxrsAdapter;
import org.jepria.oauth.client.rest.ClientJaxrsAdapter;
import org.jepria.oauth.clienturi.rest.ClientUriJaxrsAdapter;
import org.jepria.oauth.main.exception.ExtendedExceptionMapper;
import org.jepria.oauth.main.security.AllowAllOriginFilter;
import org.jepria.oauth.main.security.ClientCredentialsRequestFilter;
import org.jepria.oauth.token.rest.TokenJaxrsAdapter;
import org.jepria.server.service.rest.jersey.ApplicationConfigBase;

public class Application extends ApplicationConfigBase {
  
  public Application() {
    register(AuthorizationJaxrsAdapter.class);
    register(AuthenticationJaxrsAdapter.class);
    register(TokenJaxrsAdapter.class);
    register(ClientJaxrsAdapter.class);
    register(ClientUriJaxrsAdapter.class);
    register(ClientCredentialsRequestFilter.class);
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
    register(ExtendedExceptionMapper.class);
  }
}