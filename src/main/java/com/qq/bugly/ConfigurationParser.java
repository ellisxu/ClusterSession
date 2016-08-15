package com.qq.bugly;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationParser {
  private static Logger log = LoggerFactory.getLogger(Constant.LOG_CONFIG);
  private PropertiesConfiguration config = null;
  private static String defaultPropertName = "application.properties";

  private static class SingletonContainer {
    private static ConfigurationParser INSTANCE = new ConfigurationParser();
  }

  public static synchronized ConfigurationParser initInstance(String filePath) {
    if (StringUtils.isNotBlank(filePath)) {
      defaultPropertName = filePath;
    }

    return SingletonContainer.INSTANCE;
  }

  public static synchronized ConfigurationParser getInstance() {
    return SingletonContainer.INSTANCE;
  }

  private ConfigurationParser() {
    try {
      config = new PropertiesConfiguration(defaultPropertName);
    } catch (ConfigurationException e) {
      log.error("An error occured while attempting to initiate ConfigurationParser: {}",
          e.getMessage(), e);
    }
  }

  public Configuration getConfig() {
    return config;
  }

  public void setProperty(String key, String value) {
    config.setProperty(key, value);
  }

  public void reloadConfig() {
    try {
      config = new PropertiesConfiguration(defaultPropertName);
      config.reload();
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public HashMap<String, String> getConfigMap() {
    HashMap<String, String> keyMap = new HashMap<String, String>();
    if (config != null) {
      Iterator<String> keyIt = config.getKeys();
      while (keyIt != null && keyIt.hasNext()) {

        String key = keyIt.next();
        keyMap.put(key, config.getString(key));
      }
    }
    return keyMap;
  }

  public void setConfigMap(HashMap<String, String> map) {
    if (map != null) {
      map.keySet();
    }

  }
}
