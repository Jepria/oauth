package org.jepria.oauth;

import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.Properties;

public class JaxRsAdapterTestBase {
  
  protected static Properties properties;
  protected static String baseUrl;
  protected static String username;
  protected static String password;
  
  @BeforeAll
  public static void initialize() throws IOException {
    properties = new Properties();
    properties.load(JaxRsAdapterTestBase.class.getClassLoader().getResourceAsStream("test.properties"));
    baseUrl = properties.getProperty("server.url") + properties.getProperty("server.context-path");
    username = properties.getProperty("user.name");
    password = properties.getProperty("user.password");
  }
  
}
