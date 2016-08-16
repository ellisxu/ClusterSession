package com.qq.bugly;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
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

  public MongoDBSession(boolean create, HttpServletRequest request, HttpServletResponse response,
      ServletContext servletContext) {
    super(create, request, response, servletContext);
  }

  public MongoDBSession(boolean create, HttpServletRequest request, HttpServletResponse response,
      ServletContext servletContext, int maxInactiveInterval) {
    super(create, request, response, servletContext);
    this.setMaxInactiveInterval(maxInactiveInterval);
  }

  @Override
  public boolean set(String key, Object value) {
    long start = System.currentTimeMillis();
    boolean result = false;

    MongoCollection<Document> collection = MongoDbBridge.getInstance().getCollection();
    if (collection != null) {
      ByteArrayOutputStream outputStream = null;
      ObjectOutputStream out = null;

      try {
        Document filter = new Document();
        filter.append("_id", key);
        FindIterable<Document> iterable = collection.find(filter);
        MongoCursor<Document> cursor = iterable.iterator();

        outputStream = new ByteArrayOutputStream();
        out = new ObjectOutputStream(outputStream);
        out.writeObject(value);
        filter.append(Constant.MONGODB_COLUMN_SESSION, encode(outputStream.toByteArray()));
        if (cursor.hasNext()) {
          collection.replaceOne(Filters.eq("_id", filter.get("_id")), filter);
        } else {
          collection.insertOne(filter);
        }

        result = true;
      } catch (Exception e) {
        ClusterSessionException ce = new ClusterSessionException(e);
        log.error("An error occurred while attempting to set an object into mongodb: {}",
            ce.getMessage(), ce);
        result = false;
      } finally {
        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            log.error("An error occurred while attempting to set an object into mongodb: {}",
                e.getMessage(), e);
          }
        }
        if (outputStream != null) {
          try {
            outputStream.close();
          } catch (IOException e) {
            log.error("An error occurred while attempting to set an object into mongodb: {}",
                e.getMessage(), e);
          }
        }
      }
    } else {
      ClusterSessionException e =
          new ClusterSessionException(Constant.EXCEPTION_NONE_MONGODB_COLLECTION);
      log.error("An error occurred while attempting to set an object into mongodb: {}",
          e.getMessage(), e);
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
    if (collection != null) {
      InputStream inputStream = null;
      ObjectInputStream in = null;
      byte[] data = null;

      try {
        Document filter = new Document();
        filter.append("_id", key);
        FindIterable<Document> iterable = collection.find(filter);
        MongoCursor<Document> cursor = iterable.iterator();
        if (cursor.hasNext()) {
          Document target = cursor.next();
          String value = target.getString(Constant.MONGODB_COLUMN_SESSION);
          data = decode(value);
        }

        if (data != null) {
          inputStream = new ByteArrayInputStream(data);
          in = new ObjectInputStream(inputStream);
          result = in.readObject();
        }
      } catch (Exception e) {
        ClusterSessionException ce = new ClusterSessionException(e);
        log.error("An error occurred while attempting to get an object from mongodb: {}",
            ce.getMessage(), ce);
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            log.error("An error occurred while attempting to get an object from mongodb: {}",
                e.getMessage(), e);
          }
        }
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException e) {
            log.error("An error occurred while attempting to get an object from mongodb: {}",
                e.getMessage(), e);
          }
        }
      }
    } else {
      ClusterSessionException e =
          new ClusterSessionException(Constant.EXCEPTION_NONE_MONGODB_COLLECTION);
      log.error("An error occurred while attempting to get an object from mongodb: {}",
          e.getMessage(), e);
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
    if (collection != null) {
      try {
        Document filter = new Document();
        filter.append("_id", key);
        collection.deleteMany(filter);
        result = true;
      } catch (Exception e) {
        ClusterSessionException ce = new ClusterSessionException(e);
        log.error("An error occurred while attempting to delete an object in mongodb: {}",
            ce.getMessage(), ce);
      }
    } else {
      ClusterSessionException e =
          new ClusterSessionException(Constant.EXCEPTION_NONE_MONGODB_COLLECTION);
      log.error("An error occurred while attempting to delete an object in mongodb: {}",
          e.getMessage(), e);
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

  /**
   * 将二进制数据编码为BASE64字符串
   * 
   * @param binaryData
   * @return
   */
  public static String encode(byte[] binaryData) {
    try {
      return new String(Base64.encodeBase64(binaryData), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }

  /**
   * 将BASE64字符串恢复为二进制数据
   * 
   * @param base64String
   * @return
   */
  public static byte[] decode(String base64String) {
    try {
      return Base64.decodeBase64(base64String.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }
}
