package com.qq.bugly;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorBridge {
  private static Logger log = LoggerFactory.getLogger(Constant.LOG_CONFIG);
  private MonitorInterface monitor = null;
  private static String monitorClassStr = null;
  
  private MonitorBridge() {
    if(StringUtils.isNotBlank(monitorClassStr)) {
      try {
        Class<?> monitorClass = Class.forName(monitorClassStr);
        monitor = (MonitorInterface) monitorClass.newInstance();
      } catch (Exception e) {
        ClusterSessionException ce = new ClusterSessionException(e);
        log.error("An error occurred while attempting to initiate MonitorBridge: {}", ce.getMessage(), ce);
        throw ce;
      }
    }
  }
  
  public static synchronized MonitorBridge initInstance(String monitorClass) {
    monitorClassStr = monitorClass;
    
    return SingletonHolder.INSTANCE;
  }
  
  private static class SingletonHolder {
    private static MonitorBridge INSTANCE = new MonitorBridge();
  }
  
  public static synchronized MonitorBridge getInstance() {
    return SingletonHolder.INSTANCE;
  }

  public static void monitor(String operation, String key, long duration, boolean result) {
    log.debug("Operation: {}, key: {}, result: {}, duration: {}", operation, key, result, duration);
    
    if(getInstance().getMonitor() != null) {
      getInstance().getMonitor().monitor(operation, key, duration, result); 
    }
  }

  public MonitorInterface getMonitor() {
    return monitor;
  }
  
//  public static void main(String[] args) {
//    MonitorBridge.initInstance("com.qq.bugly.TestMonitor");
//    MonitorBridge.monitor("test", "testkey", 111, true);
//  }
}
