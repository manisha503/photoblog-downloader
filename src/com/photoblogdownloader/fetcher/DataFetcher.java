package com.photoblogdownloader.fetcher;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

/**
 * This class dispatches HTTP requests using a
 * background thread. If a callback object is provided, it will be notified when
 * the dispatching is complete.
 */
public class DataFetcher {
  /**
   * Error Codes
   */
  public static final int NO_ERROR = 0;
  /** Generic error code */
  public static final int GENERIC_ERROR = 1;
  
  /** Milliseconds to wait for connection to be established before timing out */
  private static final int MILLIS_CONNECTION_TIMEOUT = 5000;
  /** Milliseconds to wait for next data packet before timing out */
  private static final int MILLIS_SOCKET_TIMEOUT = 30000;
  /** Size of the socket's internal buffer (in bytes) */
  private static final int SOCKET_BUFFER_SIZE = 4096;
  
  private final HttpClient httpClient;
  
  public DataFetcher(HttpClient httpClient) {
    this.httpClient = httpClient;
  }
  
  /**
   * Makes a synchronous HTTP request corresponding to the given DataRequest.
   */
  public DataResponse fetch(String url) {
    byte[] rawBytes = null;
    int err = 0;
    
    if (err == NO_ERROR) {
      try {
        if (url != null) {
          // Create HTTP requests from the service request
          HttpUriRequest httpRequest = new HttpGet(url);
          
          HttpResponse httpResponse = httpClient.execute(httpRequest);
          
          HttpEntity httpEntity = httpResponse.getEntity();
          
          if (httpEntity != null) {
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(httpEntity);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
              bufferedHttpEntity.writeTo(outputStream);
              rawBytes = outputStream.toByteArray();
            } finally {
              outputStream.close();
            }
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
        err = GENERIC_ERROR;
      }
    }
    
    DataResponse response;
    if (err != NO_ERROR) {
      response = new DataResponse(false, err, null);
    } else {
      response = new DataResponse(true, err, rawBytes);
    }
    
    return response;
  }
  
  /**
   * Creates a DataFetcher instance using the default configuration settings
   */
  public static DataFetcher createDataFetcher() {
    
    DefaultHttpClient httpClient = new DefaultHttpClient();
    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
        MILLIS_CONNECTION_TIMEOUT);
    httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, MILLIS_SOCKET_TIMEOUT);
    httpClient.getParams().setParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, SOCKET_BUFFER_SIZE);
    
    return new DataFetcher(httpClient);
  }
}
