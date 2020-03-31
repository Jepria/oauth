package org.jepria.oauth.key.rest;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.key.KeyServerFactory;
import org.jepria.oauth.model.key.KeyService;
import org.jepria.oauth.model.key.dto.KeyDto;
import org.jepria.oauth.model.token.dto.TokenDto;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.HttpBasic;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;
import java.util.Date;

import static org.jepria.oauth.sdk.OAuthConstants.*;

/**
 * Authentication Endpoint takes care of authentication business logic part for Authorization Code Flow and Implicit Flow.
 */
@Path("/key")
@HttpBasic(passwordType = HttpBasic.PASSWORD)
public class KeyJaxrsAdapter extends JaxrsAdapterBase {
  
  KeyService service = KeyServerFactory.getInstance().getService();
  
  @GET
  @Path("{kid}")
  public Response getKeyById(@PathParam("kid") String keyId) {
    KeyDto keyDto = service.getKeys(keyId, securityContext.getCredential());
    if (keyDto != null) {
      return Response.ok(keyDto.getPublicKey()).build();
    } else {
      return null;
    }
  }
  
  @GET
  public Response getKey() {
    KeyDto keyDto = service.getKeys(null, securityContext.getCredential());
    if (keyDto != null) {
      return Response.ok(keyDto.getPublicKey()).build();
    } else {
      return null;
    }
  }
  
  @POST
  public Response updateKeys() {
    service.setKeys(securityContext.getCredential());
    return Response.ok().build();
  }

}
