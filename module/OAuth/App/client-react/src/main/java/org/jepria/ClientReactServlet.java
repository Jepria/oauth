package org.jepria;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

public class ClientReactServlet implements Filter {
  
  public void init(FilterConfig filterConfig) throws ServletException {
  
  }
  
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    String requestPath = ((HttpServletRequest) servletRequest).getRequestURI();
    if (!requestPath.contains(".")) {
      if (requestPath.endsWith("/")) {
        File file = new File(requestPath + "index.html");
        if (file.exists()) {
          filterChain.doFilter(servletRequest, servletResponse);
          return;
        }
      } else {
        File file = new File(requestPath + ".html");
        if (file.exists()) {
          filterChain.doFilter(servletRequest, servletResponse);
          return;
        }
      }
    } else {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }
    servletRequest.getServletContext().getRequestDispatcher("/index.jsp").forward(servletRequest, servletResponse);
    return;
  }
  
  public void destroy() {
  
  }
}
