package org.jepria.oauth.main;

import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.sdk.token.Signer;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.server.env.EnvironmentPropertySupport;
import org.jepria.server.service.security.Credential;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.main.OAuthConstants.*;
import static org.jepria.oauth.main.OAuthConstants.OAUTH_SSO_TOKEN_LIFE_TIME;
import static org.jepria.oauth.sdk.OAuthConstants.SERVER_ERROR;

public class Utils {
  
  public static final Credential serverCredential = new Credential() {
    @Override
    public int getOperatorId() {
      return 1;
    }
    
    @Override
    public String getUsername() {
      return "SERVER";
    }
    
    @Override
    public boolean isUserInRole(String roleShortName) {
      return true;
    }
  };
  
  public static boolean isValidUri(String redirectUri) {
    if (redirectUri == null) {
      return false;
    }
    try {
      new URI(redirectUri);
      return true;
    } catch (URISyntaxException | NullPointerException e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public static String getSeparator(String uri) {
    String separator = "";
    if (uri != null) {
      if (uri.contains("?")) {
        separator = "&";
      } else {
        separator = "?";
      }
    }
    return separator;
  }
  
  public static String getHostContextPath(HttpServletRequest request) {
    return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
  }
  
  public static Token generateToken(String username,
                                    List<String> audience,
                                    Integer userId,
                                    String issuer,
                                    String privateKeyString,
                                    Long expiresIn,
                                    String scope) {
    try {
      /**
       * Generate uuid for token ID
       */
      String tokenId = UUID.randomUUID().toString().replaceAll("-", "");
      /**
       * Create token with JWT lib
       */
      Token token = new TokenImpl(tokenId, audience != null ? audience : Collections.singletonList("RFInfo"), userId.toString(),
        issuer, addTime(new Date(), expiresIn), new Date(), username, scope);
      /**
       * Sign token with private key
       */
      Signer signer = new SignerRSA(privateKeyString);
      token = signer.sign(token);
      return token;
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }
  
  private static Date addTime(Date date, long seconds) {
    return new Date(date.getTime() + TimeUnit.SECONDS.toMillis(seconds));
  }
  
  public static Long getAccessTokenLifeTime(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    String tokenLifeTime = null;
    if (session != null) {
      tokenLifeTime = (String) session.getAttribute(OAUTH_ACCESS_TOKEN_LIFE_TIME);
    }
    if (tokenLifeTime == null) {
      tokenLifeTime = EnvironmentPropertySupport.getInstance(request).getProperty(OAUTH_ACCESS_TOKEN_LIFE_TIME, OAUTH_ACCESS_TOKEN_LIFE_TIME_DEFAULT);
      if (session != null) {
        session.setAttribute(OAUTH_ACCESS_TOKEN_LIFE_TIME, tokenLifeTime);
      }
    }
    return Long.valueOf(tokenLifeTime);
  }
  
  public static Long getSessionTokenLifeTime(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    String tokenLifeTime = null;
    if (session != null) {
      tokenLifeTime = (String) session.getAttribute(OAUTH_SSO_TOKEN_LIFE_TIME);
    }
    if (tokenLifeTime == null) {
      tokenLifeTime = EnvironmentPropertySupport.getInstance(request).getProperty(OAUTH_SSO_TOKEN_LIFE_TIME, OAUTH_SSO_TOKEN_LIFE_TIME_DEFAULT);
      if (session != null) {
        session.setAttribute(OAUTH_SSO_TOKEN_LIFE_TIME, tokenLifeTime);
      }
    }
    return Long.valueOf(tokenLifeTime);
  }
  
  public static Long getRefreshTokenLifeTime(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    String tokenLifeTime = null;
    if (session != null) {
      tokenLifeTime = (String) session.getAttribute(OAUTH_REFRESH_TOKEN_LIFE_TIME);
    }
    if (tokenLifeTime == null) {
      tokenLifeTime = EnvironmentPropertySupport.getInstance(request).getProperty(OAUTH_REFRESH_TOKEN_LIFE_TIME, OAUTH_REFRESH_TOKEN_LIFE_TIME_DEFAULT);
      if (session != null) {
        session.setAttribute(OAUTH_REFRESH_TOKEN_LIFE_TIME, tokenLifeTime);
      }
    }
    return Long.valueOf(tokenLifeTime);
  }
}
