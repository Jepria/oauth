package org.jepria.login.servlet;

import com.google.gson.Gson;
import org.jepria.compat.server.db.Db;
import org.jepria.login.dto.LoginConfirmDto;
import org.jepria.server.env.EnvironmentPropertySupport;
import org.jepria.server.service.security.oauth.OAuthDbHelper;
import org.jepria.server.service.security.pkg_Operator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.Base64;

import static org.jepria.oauth.sdk.OAuthConstants.CLIENT_ID_PROPERTY;

public class LoginServlet extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    String userName = req.getParameter("username");
    String password = req.getParameter("password");
    
    Integer operatorId;
    try {
      Db db = new Db("jdbc/RFInfoDS");
      operatorId = pkg_Operator.logon(db, userName, password, null);
      db.closeAll();
    } catch (SQLException exception) {
      throw new ServletException(exception);
    }
    
    LoginConfirmDto loginConfirmDto = new LoginConfirmDto();
    loginConfirmDto.setClientId(req.getParameter("client_id"));
    loginConfirmDto.setOperatorId(operatorId);
    loginConfirmDto.setRedirectUri(req.getParameter("redirect_uri"));
    loginConfirmDto.setResponseType(req.getParameter("response_type"));
    loginConfirmDto.setState(req.getParameter("state"));
    loginConfirmDto.setUsername(userName);
    
    URL callUrl =
      URI.create(req.getScheme() + "://" + req.getServerName() + (req.getServerPort() != -1 ?
        (":" + req.getServerPort()) : "") +
        "/oauth/api/session/" + req.getParameter(
        "session_id")).toURL();
    
    HttpURLConnection conn = (HttpURLConnection) callUrl.openConnection();
    
    conn.setRequestMethod("PUT");
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(0);
    conn.setInstanceFollowRedirects(false);
    String clientId = getServletContext().getInitParameter(CLIENT_ID_PROPERTY);
    String clientSecret;
    try {
      Db db = new Db("jdbc/OAuthDS");
      clientSecret = OAuthDbHelper.getClientSecret(db, clientId);
      db.closeAll();
    } catch (SQLException exception) {
      throw new ServletException(exception);
    }
    conn.setRequestProperty("Authorization", "Basic " +
      Base64.getUrlEncoder().withoutPadding().encodeToString((clientId + ":" + clientSecret).getBytes()));
    conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
    conn.setUseCaches(false);
    
    conn.setDoOutput(true);
    try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
      Gson gson = new Gson();
      writer.write(gson.toJson(loginConfirmDto));
    }
    int statusCode;
    
    try {
      statusCode = conn.getResponseCode();
    } catch (IOException e) {
      // HttpUrlConnection will throw an IOException if any
      // 4XX response is sent. If we request the status
      // again, this time the internal status will be
      // properly set, and we'll be able to retrieve it.
      statusCode = conn.getResponseCode();
      if (statusCode == -1) {
        throw e; // Rethrow IO exception
      } else {
        throw new IOException("HTTP Response Status Code: " + statusCode + '\n' + e.getLocalizedMessage());
      }
    }
    if (statusCode == 302) {
      resp.sendRedirect(conn.getHeaderField("Location"));
    }
  }
}
