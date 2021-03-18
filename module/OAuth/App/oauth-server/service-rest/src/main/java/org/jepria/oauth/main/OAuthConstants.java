package org.jepria.oauth.main;

public class OAuthConstants {
  public static final String OAUTH_ACCESS_TOKEN_LIFE_TIME = "OAUTH_ACCESS_TOKEN_LIFE_TIME";
  public static final String OAUTH_ACCESS_TOKEN_LIFE_TIME_DEFAULT = "3600";
  public static final String OAUTH_REFRESH_TOKEN_LIFE_TIME = "OAUTH_REFRESH_TOKEN_LIFE_TIME";
  public static final String OAUTH_REFRESH_TOKEN_LIFE_TIME_DEFAULT = "28800";
  public static final String OAUTH_SSO_TOKEN_LIFE_TIME_DEFAULT = "86400";
  public static final String OAUTH_SSO_TOKEN_LIFE_TIME = "OAUTH_SSO_TOKEN_LIFE_TIME";
  public static final String DEFAULT_LOGIN_MODULE = "/oauth/login";
  public static final String LOGIN_MODULE = "OAUTH_LOGIN_MODULE";
  /**
   * Query params
   */
  public static final String ERROR_QUERY_PARAM = "error";
  public static final String ERROR_ID_QUERY_PARAM = "error_id";
  public static final String ERROR_DESCRIPTION_QUERY_PARAM = "error_description";
  public static final String ACCESS_TOKEN_QUERY_PARAM = "access_token";
  public static final String TOKEN_TYPE_QUERY_PARAM = "token_type";
  public static final String EXPIRES_IN_QUERY_PARAM = "expires_in";
  public static final String SID = "session_id";
}
