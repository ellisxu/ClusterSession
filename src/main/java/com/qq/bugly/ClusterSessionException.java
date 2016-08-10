package com.qq.bugly;

public class ClusterSessionException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 8070274570408580028L;
  
  public ClusterSessionException(String message) {
    super(message);
  }
  
  public ClusterSessionException(Throwable cause) {
    super(cause);
  }
}
