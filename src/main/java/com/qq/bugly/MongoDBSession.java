package com.qq.bugly;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
/**
 * 
 * @author ellis
 *
 */
public class MongoDBSession extends ClusterSession {

  public MongoDBSession(boolean create, HttpServletRequest request,
      HttpServletResponse response, ServletContext servletContext) {
    super(create, request, response, servletContext);
  }
  
  public MongoDBSession(boolean create, HttpServletRequest request,
      HttpServletResponse response, ServletContext servletContext, int maxInactiveInterval) {
    super(create, request, response, servletContext);
    this.setMaxInactiveInterval(maxInactiveInterval); 
  }
  
  @Override
  public boolean set(String key, Object value){
    long start = System.currentTimeMillis();
    boolean result = false;
    
    MongoCollection<Document> collection = MongoDbBridge.getInstance().getCollection();
    if(collection != null) {
      try {
        Document filter = new Document();
        filter.append("_id", key);
        FindIterable<Document> iterable = collection.find(filter);
        MongoCursor<Document> cursor = iterable.iterator();
        
        filter.append(Constant.MONGODB_COLUMN_SESSION, value);
        if (cursor.hasNext()) {
          collection.replaceOne(Filters.eq("_id", filter.get("_id")), filter);
        } else {
          collection.insertOne(filter); 
        }
        
        result = true;
      } catch (Exception e) {
        ClusterSessionException ce = new ClusterSessionException(e);
        log.error("An error occurred while attempting to set an object into mongodb: {}", ce.getMessage(), ce);
        result = false;
      }
    } else {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_NONE_MONGODB_COLLECTION);
      log.error("An error occurred while attempting to set an object into mongodb: {}", e.getMessage(), e);
      result = false;
    }
    long end = System.currentTimeMillis();
    
    log.debug("set-duration: {}", end - start); 
    
    return result;
  }
  
  @Override
  public Object get(String key) {
    long start = System.currentTimeMillis();
    Object result = null;
    
    MongoCollection<Document> collection = MongoDbBridge.getInstance().getCollection();
    if(collection != null) {
      try {
        Document filter = new Document();
        filter.append("_id", key);
        FindIterable<Document> iterable = collection.find(filter);
        MongoCursor<Document> cursor = iterable.iterator();
        if (cursor.hasNext()) {
          Document target = cursor.next();
          result = target.get(Constant.MONGODB_COLUMN_SESSION);
        }
      } catch (Exception e) {
        ClusterSessionException ce = new ClusterSessionException(e);
        log.error("An error occurred while attempting to get an object from mongodb: {}", ce.getMessage(), ce);
      }
    } else {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_NONE_MONGODB_COLLECTION);
      log.error("An error occurred while attempting to get an object from mongodb: {}", e.getMessage(), e);
    }
    
    long end = System.currentTimeMillis();
    
    log.debug("get-duration: {}", end - start); 
    
    return result;
  }
  
  @Override
  public boolean delete(String key) {
    long start = System.currentTimeMillis();
    boolean result = false;
    
    MongoCollection<Document> collection = MongoDbBridge.getInstance().getCollection();
    if(collection != null) {
      try {
        Document filter = new Document();
        filter.append("_id", key);
        collection.deleteMany(filter);
        result = true;
      } catch (Exception e) {
        ClusterSessionException ce = new ClusterSessionException(e);
        log.error("An error occurred while attempting to delete an object in mongodb: {}", ce.getMessage(), ce);
      }
    } else {
      ClusterSessionException e = new ClusterSessionException(Constant.EXCEPTION_NONE_MONGODB_COLLECTION);
      log.error("An error occurred while attempting to delete an object in mongodb: {}", e.getMessage(), e);
    }
    
    long end = System.currentTimeMillis();
    
    log.debug("delete-duration: {}", end - start); 
    
    return result;
  }
  
  @Override
  public String getSessionCackeKey() {
    String cacheKey = this.sessionCookieId + "." + this.id;
    
    log.debug("Get session-cache key: {}", cacheKey); 
    
    return cacheKey;
  }
}
