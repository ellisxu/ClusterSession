package com.qq.bugly;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.Bytes;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;

/**
 * 
 * @author ellis
 *
 */
public class MongoDbBridge {
  private Logger log = LoggerFactory.getLogger(Constant.LOG_CONFIG);
  
  private static final int CON_TIMEOUT = 60000;//connection timeout: 60 seconds
  
  private static final int SOCKET_TIMEOUT = 10000;//socket timeout: 10 seconds
  
  private static MongoClient mc = null;

  private static final int MAX_MONGO_CONNECTION_COUNT = 5;
  
  private static String mongodbSvrPort;
  
  private static String dbName = Constant.MONGODB_DB_NAME;
  
  private static String collectionName = Constant.MONGODB_COLLECTION_NAME;
  
  private MongoDbBridge() {
    mc = connect();
  }
  
  private static class SingletonHolder {
    private static MongoDbBridge INSTANCE = new MongoDbBridge();
  }
  
  public static synchronized MongoDbBridge initInstance(String hostservers, String db, String collection) {
    mongodbSvrPort = hostservers;
    if(StringUtils.isNotBlank(db)) {
      dbName = db;
    }
    if(StringUtils.isNotBlank(collection)) {
      collectionName = collection;
    }
    
    return SingletonHolder.INSTANCE;
  }
  
  public static synchronized MongoDbBridge getInstance() {
    return SingletonHolder.INSTANCE;
  }
  
  @SuppressWarnings("deprecation")
  private MongoClient connect() {
    String[] svrs = mongodbSvrPort.split(";");
    List<ServerAddress> sas = new ArrayList<ServerAddress>();
    for (String address : svrs)
        sas.add(new ServerAddress(address));
    MongoClientOptions mco =
        MongoClientOptions.builder().connectionsPerHost(MAX_MONGO_CONNECTION_COUNT)
            .connectTimeout(CON_TIMEOUT).cursorFinalizerEnabled(true)
            .threadsAllowedToBlockForConnectionMultiplier(8).writeConcern(new WriteConcern(1)).socketTimeout(SOCKET_TIMEOUT)
            .build();
    MongoClient mc = new MongoClient(sas, mco);
    
    mc.setOptions(Bytes.QUERYOPTION_NOTIMEOUT);
//    mc.setOptions(Bytes.QUERYOPTION_SLAVEOK);
//    mc.setReadPreference(ReadPreference.secondaryPreferred());
    log.info("connect mongodb success!,mongodbSvrPort:{}", mongodbSvrPort);
    return mc;
  }
  
  public MongoCollection<Document> getCollection() {
    MongoCollection<Document> collection =  mc.getDatabase(dbName).getCollection(collectionName);
    return collection;
  }
}
