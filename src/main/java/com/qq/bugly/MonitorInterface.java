package com.qq.bugly;

public interface MonitorInterface {
  public void monitor(String operation, String key, long duration, boolean result);
}
