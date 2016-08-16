package com.qq.bugly;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public abstract class ClusterSession implements HttpSession {
  protected Logger log = LoggerFactory.getLogger(Constant.LOG_CONFIG);

  protected String sessionCookieId = Constant.SESSION_ID;
  protected String id;
  protected ServletContext servletContext;
  protected Map<String, Object> attributes = null;
  protected Long creationTime = 0L;
  protected int maxInactiveInterval = 3600;
  protected boolean isnew = false;
  protected Map<String, Object> currentClusterAttributesCache = null;

  @SuppressWarnings("unchecked")
  public ClusterSession(boolean create, HttpServletRequest request,
      HttpServletResponse response, ServletContext servletContext) {
    if(request == null) {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_HTTPSERVLETREQUEST_NULL);
      log.error("An error occured while attempting to initiate a MongoDBSession object: {}", e.getMessage(), e); 
      throw e;
    }
    if(response == null) {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_HTTPSERVLETRESPONSE_NULL);
      log.error("An error occured while attempting to initiate a MongoDBSession object: {}", e.getMessage(), e); 
      throw e;
    }
    if(servletContext == null) {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_SERVLETCONTEXT_NULL);
      log.error("An error occured while attempting to initiate a MongoDBSession object: {}", e.getMessage(), e); 
      throw e;
    }
    
    this.servletContext = servletContext;
    
    Cookie[] cookies = request.getCookies();
    if (cookies != null && cookies.length > 0) {
      for (Cookie cookie : cookies) {
        if(sessionCookieId.equals(cookie.getName())) {
          this.id = cookie.getValue();
          
          if (StringUtils.isNotBlank(this.id)) {
            Object cache = get(this.getSessionCackeKey());
            if (cache != null) {
              try {
                this.attributes = (Map<String, Object>) cache;
                Object creationTimeCache = this.attributes.get(Constant.CREATION_TIME_KEY);
                if (creationTimeCache != null && creationTimeCache instanceof Long) {
                  creationTime = (Long) creationTimeCache;
                }
                if (creationTime == 0) {
                  this.id = null;
                }
              } catch (Exception e) {
                ClusterSessionException ce = new ClusterSessionException(e);
                log.error("An error occured while attempting to initiate a MongoDBSession object: {}", ce.getMessage(), ce);
                throw ce;
              }
            } else {
              this.id = null;
            }
          } else {
            this.id = null;
          }
          
          break;
        }
      }
    }
    
    if (StringUtils.isBlank(this.id) && create) {
      this.id = UUID.randomUUID().toString();
      Cookie cookie = new Cookie(sessionCookieId, this.id);
      cookie.setPath("/");
      cookie.setMaxAge(-1);
      response.addCookie(cookie);
      
      this.creationTime = System.currentTimeMillis();
      this.attributes = new HashMap<>();
      this.attributes.put(Constant.CREATION_TIME_KEY, this.creationTime);
      this.attributes.put(Constant.LAST_ASSESSED_TIME_KEY, this.creationTime);
      this.attributes.put(Constant.ATTRIBUTE_NAMES_KEY, new ArrayList<String>());
      boolean result = set(getSessionCackeKey(), this.attributes);
      this.isnew = true;
      
      if(!result) {
        ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_STORENEWSESSION);
        log.error("An error occured while attempting to initiate a MongoDBSession object: {}", e.getMessage(), e); 
        throw e;
      }
    }
    
    if(StringUtils.isNotBlank(this.id)) {
      this.currentClusterAttributesCache = this.getAttributes();
    }
    
    log.debug("Initiated a session successfully. session id: {}", this.id);
  }
  
  public abstract boolean set(String key, Object value);

  public abstract Object get(String key);

  public abstract boolean delete(String key);

  public abstract String getSessionCackeKey();

  @Override
  public long getCreationTime() {
    log.debug("creationTime: {}", this.creationTime);

    return this.creationTime;
  }

  @Override
  public String getId() {
    log.debug("getId: {}", this.id);

    return id;
  }

  @Override
  public long getLastAccessedTime() {
    Long lastAccessedTime = 0L;

    Map<String, Object> map = this.getAttributes();
    Object timeCache = map.get(Constant.LAST_ASSESSED_TIME_KEY);
    if (timeCache != null && timeCache instanceof Long) {
      lastAccessedTime = (Long) timeCache;
    } else {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_CACHE);
      log.error("An error occured while attempting to get the last accessed-time: {}",
          e.getMessage(), e);
      throw e;
    }

    log.debug("lastAccessedTime: {}", lastAccessedTime);

    return lastAccessedTime;
  }

  @Override
  public ServletContext getServletContext() {
    return this.servletContext;
  }

  @Override
  public void setMaxInactiveInterval(int interval) {
    log.debug("setMaxInactiveInterval: {}", interval);

    this.maxInactiveInterval = interval;
  }

  @Override
  public int getMaxInactiveInterval() {
    log.debug("getMaxInactiveInterval: {}", this.maxInactiveInterval);

    return this.maxInactiveInterval;
  }

  @Deprecated
  @Override
  public HttpSessionContext getSessionContext() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getAttribute(String name) {
    if (this.currentClusterAttributesCache != null) {
      Map<String, Object> map = this.currentClusterAttributesCache;

      Object result = null;
      map.put(Constant.LAST_ASSESSED_TIME_KEY, System.currentTimeMillis());
      this.setAttributes(map);

      result = map.get(name);


      log.debug("getAttribute, name: {}, result: {}", name, result);

      return result;
    } else {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_CACHE);
      log.error("An error occured while attempting to get an attribute: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Deprecated
  @Override
  public Object getValue(String name) {
    return null;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Enumeration getAttributeNames() {
    if (this.currentClusterAttributesCache != null) {
      Map<String, Object> map = this.currentClusterAttributesCache;
      Enumeration<String> result = null;

      Object namesCache = map.get(Constant.ATTRIBUTE_NAMES_KEY);
      final List<String> names = new ArrayList<>();

      if (namesCache != null) {
        try {
          List<String> list = (List<String>) namesCache;
          names.addAll(list);
        } catch (Exception e) {
          ClusterSessionException ce = new ClusterSessionException(e);
          log.error("An error occured while attempting to get attribute names: {}", ce.getMessage(),
              ce);
          throw ce;
        }
      } else {
        ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_CACHE);
        log.error("An error occured while attempting to get attribute names: {}", e.getMessage(),
            e);
        throw e;
      }
      map.put(Constant.LAST_ASSESSED_TIME_KEY, System.currentTimeMillis());

      result = new Enumeration<String>() {
        private List<String> nameList = names;
        private int index = 0;
        private int size = names.size();

        @Override
        public boolean hasMoreElements() {
          if (index < size) {
            return true;
          }
          return false;
        }

        @Override
        public String nextElement() {
          if (index >= size) {
            ClusterSessionException e =
                new ClusterSessionException("Out of bounds. index: " + index + ", size: " + size);
            throw e;
          }

          String element = nameList.get(index);
          ++index;

          return element;
        }
      };

      log.debug("getAttributeNames: {}", result);

      return result;
    } else {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_CACHE);
      log.error("An error occured while attempting to get attribute names: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Deprecated
  @Override
  public String[] getValueNames() {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setAttribute(String name, Object value) {
    log.debug("setAttribute, name: {}, value: {}", name, value);

    if (this.currentClusterAttributesCache != null) {
      if (Constant.ATTRIBUTE_NAMES_KEY.equals(name) || Constant.CREATION_TIME_KEY.equals(name)
          || Constant.LAST_ASSESSED_TIME_KEY.equals(name)) {
        ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_RESERVED_KEY);
        log.error("An error occured while attempting to set an attribute: {}", e.getMessage(), e);
        throw e;
      }

      Map<String, Object> map = this.currentClusterAttributesCache;

      Object namesCache = map.get(Constant.ATTRIBUTE_NAMES_KEY);
      if (namesCache != null) {
        try {
          List<String> names = (List<String>) namesCache;
          names.add(name);
          map.put(name, value);
          map.put(Constant.ATTRIBUTE_NAMES_KEY, names);
          map.put(Constant.LAST_ASSESSED_TIME_KEY, System.currentTimeMillis());
        } catch (Exception e) {
          ClusterSessionException ce = new ClusterSessionException(e);
          log.error("An error occured while attempting to set an attribute: {}", ce.getMessage(), ce);
          throw ce;
        }
      } else {
        ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_CACHE);
        log.error("An error occured while attempting to set an attribute: {}", e.getMessage(), e);
        throw e;
      }
    } else {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_CACHE);
      log.error("An error occured while attempting to set an attribute: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Deprecated
  @Override
  public void putValue(String name, Object value) {}

  @SuppressWarnings("unchecked")
  @Override
  public void removeAttribute(String name) {
    log.debug("removeAttribute, name: {}", name);

    if (this.currentClusterAttributesCache != null) {
      if (Constant.ATTRIBUTE_NAMES_KEY.equals(name) || Constant.CREATION_TIME_KEY.equals(name)
          || Constant.LAST_ASSESSED_TIME_KEY.equals(name)) {
        ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_RESERVED_KEY);
        log.error("An error occured while attempting to remove an attribute: {}", e.getMessage(), e);
        throw e;
      }

      Map<String, Object> map = this.currentClusterAttributesCache;
      Object namesCache = map.get(Constant.ATTRIBUTE_NAMES_KEY);
      if (namesCache != null) {

        try {
          List<String> names = (List<String>) namesCache;
          if (names.contains(name)) {
            names.remove(name);
            map.remove(name);
          }

          map.put(Constant.LAST_ASSESSED_TIME_KEY, System.currentTimeMillis());
        } catch (Exception e) {
          ClusterSessionException ce = new ClusterSessionException(e);
          log.error("An error occured while attempting to remove an attribute: {}", ce.getMessage(),
              ce);
          throw ce;
        }
      } else {
        ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_CACHE);
        log.error("An error occured while attempting to remove an attribute: {}", e.getMessage(), e);
        throw e;
      }
    } else {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_INVALID_CACHE);
      log.error("An error occured while attempting to remove an attribute: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Deprecated
  @Override
  public void removeValue(String name) {}

  @Override
  public void invalidate() {
    log.debug("invalidate");

    delete(this.getSessionCackeKey());

    this.id = null;
    this.creationTime = 0L;
    this.attributes = null;
    this.isnew = false;
    this.currentClusterAttributesCache = null;
  }

  @Override
  public boolean isNew() {
    return this.isnew;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getAttributes() {
    Object cache = get(this.getSessionCackeKey());

    if (cache != null) {
      try {
        this.attributes = (Map<String, Object>) cache;
      } catch (Exception e) {
        ClusterSessionException ce = new ClusterSessionException(e);
        log.error("An error occured while attempting to get attributes: {}", ce.getMessage(), ce);
        throw ce;
      }
    } else {
    }

    log.debug("getAttributes: {}", this.attributes);

    return this.attributes;
  }

  public void setAttributes(Map<String, Object> map) {
    log.debug("setAttributes: {}", map);

    boolean result = set(this.getSessionCackeKey(), map);
    if (result) {
      this.attributes = map;
    } else {
      ClusterSessionException ce = new ClusterSessionException(Constant.EXCEPTION_STORENEWSESSION);
      log.error("An error occured while attempting to set attributes: {}", ce.getMessage(), ce);
      throw ce;
    }
  }
  
  public void flush() {
    this.setAttributes(currentClusterAttributesCache); 
  }
}
