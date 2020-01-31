package org.jepria.oauth.main.security;

import org.jepria.server.service.security.CorsResponseFilter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import java.io.IOException;

/**
 * Перевод системного CORS Filter на подключение с помощью аннотации.
 */
@AllowAllOrigin
public class AllowAllOriginFilter extends CorsResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    super.filter(requestContext, responseContext);
  }

}
