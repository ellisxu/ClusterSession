package com.qq.bugly;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ellis
 *
 */
public class ClusterSessionFilter implements Filter {
  private Logger log = LoggerFactory.getLogger(Constant.LOG_CONFIG);

  private int maxInactiveInterval = Constant.DEFAULT_MAXINACTIVEINTERVAL;
  private String type = null;
  private ServletContext context = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    context = filterConfig.getServletContext();
    String filePath = filterConfig.getInitParameter(Constant.CONFIG_FILE);
    ConfigurationParser.initInstance(filePath);
    
    String monitorClass = ConfigurationParser.getInstance().getConfig().getString(Constant.CONFIG_MONITOR);
    MonitorBridge.initInstance(monitorClass);

    type = ConfigurationParser.getInstance().getConfig().getString(Constant.CONFIG_TYPE);
    String intervalStr = ConfigurationParser.getInstance().getConfig()
        .getString(Constant.CONFIG_MAXINACTIVEINTERVAL);
    if (StringUtils.isNotBlank(intervalStr)) {
      try {
        maxInactiveInterval = Integer.valueOf(intervalStr);
      } catch (NumberFormatException e) {
        log.error("An error occurred while parsing the MaxInactiveInterval param: {}",
            e.getMessage(), e);
      }
    }
    if (StringUtils.isBlank(type)) {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_TYPE);
      log.error("An error occurred while parsing the type param: {}", e.getMessage(), e);
      throw e;
    }
    if (!type.toLowerCase().equals(Constant.TYPE_MONGODB)) {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_TYPE);
      log.error("An error occurred while parsing the type param: {}", e.getMessage(), e);
      throw e;
    }

    if (type.toLowerCase().equals(Constant.TYPE_MONGODB)) {
      String hosts =
          ConfigurationParser.getInstance().getConfig().getString(Constant.CONFIG_MONGODB_HOST);
      if (StringUtils.isBlank(hosts)) {
        ClusterSessionException e =
            new ClusterSessionException(Constant.EXCEPTION_NONE_MONGODB_HOST);
        log.error("An error occurred while parsing the hosts param: {}", e.getMessage(), e);
        throw e;
      }
      String dbName =
          ConfigurationParser.getInstance().getConfig().getString(Constant.CONFIG_MONGODB_DB_NAME);
      String collectionName = ConfigurationParser.getInstance().getConfig()
          .getString(Constant.CONFIG_MONGODB_COLLECTION_NAME);

      MongoDbBridge.initInstance(hosts, dbName, collectionName);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequestWrapper requestWrapper =
        new HttpServletRequestWrapper((HttpServletRequest) request) {
          private HttpSession session = null;

          @Override
          public HttpSession getSession() {
            log.debug("getSession()");

            return this.getSession(true);
          }

          @Override
          public HttpSession getSession(boolean create) {
            log.debug("getSession({})", create);

            if (session == null) {
              if (type.toLowerCase().equals(Constant.TYPE_MONGODB)) {
                session = new MongoDBSession(create, (HttpServletRequest) request,
                    (HttpServletResponse) response, context, maxInactiveInterval);
                if (StringUtils.isBlank(session.getId())) {
                  session = null;
                }
              }
            }

            return session;
          }
        };

    chain.doFilter(requestWrapper, response);

    HttpSession session = requestWrapper.getSession(false);
    if (session != null && session instanceof ClusterSession) {
      ((ClusterSession) session).flush();
    }
  }

  @Override
  public void destroy() {

  }

}
