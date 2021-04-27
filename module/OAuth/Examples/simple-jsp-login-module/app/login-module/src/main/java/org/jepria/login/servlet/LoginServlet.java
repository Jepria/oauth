package org.jepria.login.servlet;

import com.google.gson.Gson;
import org.jepria.compat.server.db.Db;
import org.jepria.oauth.sdk.SessionApprovalRequest;
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
  
    URI callUrl =
      URI.create(req.getScheme() + "://" + req.getServerName() + (req.getServerPort() != -1 ?
        (":" + req.getServerPort()) : "") +
        "/oauth/api/session/" + req.getParameter(
        "session_id"));
    String clientId = getServletContext().getInitParameter(CLIENT_ID_PROPERTY);
    String clientSecret;
    try {
      Db db = new Db("jdbc/OAuthDS");
      clientSecret = OAuthDbHelper.getClientSecret(db, clientId);
      db.closeAll();
    } catch (SQLException exception) {
      throw new ServletException(exception);
    }
    
    SessionApprovalRequest sessionApprovalRequest = SessionApprovalRequest.Builder()
      .clientId(req.getParameter("client_id"))
      .loginModuleClientId(clientId)
      .loginModuleClientSecret(clientSecret)
      .operatorId(operatorId)
      .redirectionURI(URI.create(req.getParameter("redirect_uri")))
      .responseType(req.getParameter("response_type"))
      .state(req.getParameter("state"))
      .resourceURI(callUrl)
      .userName(userName).build();
    
    URI result = sessionApprovalRequest.execute();
    resp.sendRedirect(result.toString());
  }
}
