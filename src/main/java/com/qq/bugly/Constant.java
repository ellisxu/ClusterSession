package com.qq.bugly;
/**
 * 
 * @author ellis
 *
 */
public class Constant {
  public static final String LOG_CONFIG = "cluster_session_log";
  
  public static final String SESSION_ID = "cluster_jsessionid";
  public static final String CREATION_TIME_KEY = "creation_time";
  public static final String LAST_ASSESSED_TIME_KEY = "last_accessed_time";
  public static final String ATTRIBUTE_NAMES_KEY = "names";
  
  public static final String EXCEPTION_HTTPSERVLETREQUEST_NULL = "An HttpServletRequest object is required!";
  public static final String EXCEPTION_HTTPSERVLETRESPONSE_NULL = "An HttpServletResponse object is required!";
  public static final String EXCEPTION_SERVLETCONTEXT_NULL = "An ServletContext object is required!";
  public static final String EXCEPTION_STORENEWSESSION = "Failed to stored new session!";
  public static final String EXCEPTION_INVALID_CACHE = "The cache is invalid!";
  public static final String EXCEPTION_NONE_CACHE = "Can't find the valid cache!";
  public static final String EXCEPTION_RESERVED_KEY = "Can't use reserved keys!";
  public static final String EXCEPTION_INVALID_TYPE = "Invalid type!";
  public static final String EXCEPTION_NONE_MONGODB_HOST = "Can't find mongodb hosts!";
  public static final String EXCEPTION_NONE_MONGODB_COLLECTION = "Can't find the required collection!";
  
  public static final String TYPE_MONGODB = "mongodb";
  public static final String TYPE_MEMCACHED = "memcached";
  public static final String TYPE_REDIS = "redis";
  
  public static final String MONGODB_DB_NAME = "cluster_session_db";
  public static final String MONGODB_COLLECTION_NAME = "cluster_session_collection";
  public static final String MONGODB_COLUMN_SESSION = "s";

  public static final String CONFIG_FILE = "configuration";
  public static final String CONFIG_TYPE = "type";
  public static final String CONFIG_MAXINACTIVEINTERVAL = "maxInactiveInterval";
  public static final String CONFIG_MONGODB_HOST = "hosts";
  public static final String CONFIG_MONGODB_DB_NAME = "dbName";
  public static final String CONFIG_MONGODB_COLLECTION_NAME = "collectionName";
  
  public static final int DEFAULT_MAXINACTIVEINTERVAL = 3600;
  
}
