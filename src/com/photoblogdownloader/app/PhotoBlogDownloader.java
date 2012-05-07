package com.photoblogdownloader.app;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TumblrApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class PhotoBlogDownloader {
  
  public static final String TUMBLR_API_KEY = "";
  public static final String TUMBLR_API_SECRET = "";
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    OAuthService service = new ServiceBuilder().provider(TumblrApi.class).apiKey(TUMBLR_API_KEY).apiSecret(
        TUMBLR_API_SECRET).build();
    Token accessToken = service.getXAuthAccessToken("", "");
    System.out.println("GOT ACCESS TOKEN: " + accessToken.getToken());
    OAuthRequest req = new OAuthRequest(Verb.GET, TumblrConstants.API_POSTS_URL);
    req.addQuerystringParameter(TumblrConstants.API_KEY, TUMBLR_API_KEY);
    req.addQuerystringParameter(TumblrConstants.OFFSET, new Integer(0).toString());
    req.addQuerystringParameter(TumblrConstants.LIMIT, new Integer(20).toString());
    service.signRequest(accessToken, req);
    Response response = req.send();
    String resp = response.getBody();
    
    System.out.println("GOT BLOG POSTS: ");
    System.out.println(resp);
  }
  
}
