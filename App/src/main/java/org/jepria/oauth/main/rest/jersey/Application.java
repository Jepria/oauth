package org.jepria.oauth.main.rest.jersey;

import org.jepria.oauth.authentication.rest.AuthenticationJaxrsAdapter;
import org.jepria.oauth.authorization.rest.AuthorizationJaxrsAdapter;
import org.jepria.oauth.main.security.ClientCredentialsRequestFilter;
import org.jepria.oauth.token.rest.TokenJaxrsAdapter;
import org.jepria.server.service.rest.jersey.ApplicationConfigBase;

public class Application extends ApplicationConfigBase {
  
  public Application() {
    register(AuthorizationJaxrsAdapter.class);
    register(AuthenticationJaxrsAdapter.class);
    register(TokenJaxrsAdapter.class);
    register(ClientCredentialsRequestFilter.class);
  }
}