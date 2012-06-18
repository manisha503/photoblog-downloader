package com.photoblogdownloader.fetcher;

public class DataResponse {
  public boolean success;
  public int err;
  public byte[] rawData;
  
  public DataResponse(boolean success, int clientError, byte[] rawData) {
    this.success = success;
    this.err = clientError;
    this.rawData = rawData;
  }
  
}
