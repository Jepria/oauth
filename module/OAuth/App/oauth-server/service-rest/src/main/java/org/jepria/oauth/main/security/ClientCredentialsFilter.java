package org.jepria.oauth.main.security;

import org.jepria.compat.server.db.Db;
import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authentication.AuthenticationService;
import org.jepria.server.data.RuntimeSQLException;
import org.jepria.server.service.security.HttpBasicDynamicFeature;
import org.jepria.server.service.security.SecurityContext;
import org.jepria.server.service.security.oauth.OAuthDbHelper;
import org.jepria.server.service.security.pkg_Operator;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;

import static org.jepria.compat.server.JepRiaServerConstant.DEFAULT_DATA_SOURCE_JNDI_NAME;
import static org.jepria.compat.server.JepRiaServerConstant.DEFAULT_OAUTH_DATA_SOURCE_JNDI_NAME;
import static org.jepria.server.service.security.HttpBasic.PASSWORD;

@ClientCredentials
@Priority(Priorities.AUTHENTICATION)
public class ClientCredentialsFilter implements ContainerRequestFilter {
  
  @Context
  private HttpServletRequest request;
  
  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String authString = requestContext.getHeaderString("authorization");
    if (authString == null) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic").build());
      return;
    }
    try {
      authString = authString.replaceFirst("[Bb]asic ", "");
      String[] credentials = new String(Base64.getDecoder().decode(authString)).split(":");
      Db db = new Db(DEFAULT_OAUTH_DATA_SOURCE_JNDI_NAME);
      Integer operatorId;
      try {
        operatorId = OAuthDbHelper.loginByClientSecret(db, credentials[0], credentials[1]);
      } catch (Throwable ex) {
        if (ex.getMessage().contains("DataSource 'java:/comp/env/" + DEFAULT_OAUTH_DATA_SOURCE_JNDI_NAME + "' not found")) {
          db = new Db(DEFAULT_DATA_SOURCE_JNDI_NAME);
          operatorId = OAuthDbHelper.loginByClientSecret(db, credentials[0], credentials[1]);
        } else {
          throw ex;
        }
      } finally {
        db.closeAll();
      }
      if (operatorId != null) {
        requestContext.setSecurityContext(new ClientSecurityContext(request, credentials[0], operatorId));
      } else {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic").build());
      }
    } catch (Throwable th) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic").build());
      return;
    }
  }
  
  final class ClientSecurityContext extends SecurityContext {
    
    public ClientSecurityContext(HttpServletRequest request, String username, Integer operatorId) {
      super(request, username, operatorId);
    }
    
    @Override
    public boolean isUserInRole(String s) {
      Db db = new Db(DEFAULT_OAUTH_DATA_SOURCE_JNDI_NAME);
      try {
        boolean result = super.isRole(db, s);
        return result;
      } catch (SQLException exception) {
        throw new RuntimeSQLException(exception);
      } finally {
        db.closeAll();
      }
    }
    
    @Override
    public String getAuthenticationScheme() {
      return BASIC_AUTH;
    }
  }
}