package org.jepria.oauth.main.exception;

import org.jepria.oauth.authorization.rest.AuthorizationJaxrsAdapter;
import org.jepria.oauth.main.dto.ErrorDto;
import org.jepria.oauth.sdk.util.URIUtil;
import org.jepria.oauth.token.rest.TokenJaxrsAdapter;
import org.jepria.server.service.rest.jersey.ApplicationConfigBase;
import org.jepria.server.service.rest.jersey.ExceptionManager;
import org.jepria.server.service.rest.jersey.ExceptionManagerImpl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Base64;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class ExtendedExceptionMapper extends ApplicationConfigBase.ExceptionMapperDefault {

  @Context
  ResourceInfo resourceInfo;
  @Context
  UriInfo uriInfo;
  @Context
  HttpServletRequest request;
  ExceptionManager exceptionManager = new ExceptionManagerImpl();

  @Override
  public Response toResponse(Throwable exception) {
    if (resourceInfo.getResourceClass() == null) {
      super.toResponse(exception);
    }
    String errorId = exceptionManager.registerException(exception);
    String exceptionCode;
    if (exception instanceof HandledRuntimeException) {
      exceptionCode = ((HandledRuntimeException) exception).getExceptionCode();
    } else {
      exceptionCode = SERVER_ERROR;
    }
    String error = ERROR_QUERY_PARAM + exceptionCode + "&"
      + ERROR_DESCRIPTION_QUERY_PARAM + URIUtil.encodeURIComponent(exception.getMessage()) + "&"
      + ERROR_ID_QUERY_PARAM + errorId;
    if (resourceInfo.getResourceClass().equals(AuthorizationJaxrsAdapter.class)) {
      MultivaluedMap<String, String> params = uriInfo.getQueryParameters(true);
      String encodedRedirectUri = params.getFirst(REDIRECT_URI);
      String state = params.getFirst(STATE);

      if (encodedRedirectUri != null) {
        String redirectUri = new String(Base64.getUrlDecoder().decode(encodedRedirectUri));
        String escapedRedirectUri = URIUtil.encodeURIComponent(URI.create(redirectUri + getSeparator(redirectUri)
          + error + "&"
          + STATE + "=" + state).toString());
        return Response.status(302).location(
          URI.create(OAUTH_ERROR_CONTEXT_PATH + "?"
            + error + "&"
            + REDIRECT_URI + "=" + escapedRedirectUri)).build();
      } else {
        return Response.status(302).location(
          URI.create(OAUTH_ERROR_CONTEXT_PATH + "?" + error)).build();
      }
    } else if (resourceInfo.getResourceClass().equals(TokenJaxrsAdapter.class)) {
      ErrorDto errorDto = new ErrorDto();
      errorDto.setErrorId(errorId);
      errorDto.setError(exceptionCode);
      errorDto.setErrorDescription(exception.getMessage());
      errorDto.setErrorUri(URI.create(request.getRequestURL().toString().replaceFirst(request.getRequestURI(), OAUTH_ERROR_CONTEXT_PATH) + "?" + error).toString());

      switch (exceptionCode) {
        case SERVER_ERROR: {
          return Response.serverError().entity(errorDto).build();
        }
        case ACCESS_DENIED: {
          return Response.status(Response.Status.UNAUTHORIZED).entity(errorDto).build();
        }
        default: {
          return Response.status(Response.Status.BAD_REQUEST).entity(errorDto).build();
        }
      }
    } else {
      return super.toResponse(exception);
    }
  }

  /**
   * Get next separator for URI
   *
   * @param uri
   * @return
   */
  private static String getSeparator(String uri) {
    String separator = "";
    if (uri != null) {
      if (uri.contains("?")) {
        separator = "&";
      } else if (uri.endsWith("/")) {
        separator = "?";
      } else {
        separator = "/?";
      }
    }
    return separator;
  }
}
