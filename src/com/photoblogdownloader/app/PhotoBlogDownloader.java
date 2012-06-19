package com.photoblogdownloader.app;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TumblrApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.photoblogdownloader.fetcher.DataFetcher;
import com.photoblogdownloader.fetcher.DataResponse;

public class PhotoBlogDownloader {
  
  public static final String TUMBLR_API_KEY = "";
  public static final String TUMBLR_API_SECRET = "";
  private static final int FETCH_LIMIT = 20;
  
  private static Token accessToken;
  private static OAuthService service;
  private static String imageDir;
  private static DataFetcher fetcher;
  private static String postIdsFilename;
  private static HashSet<Long> postIdsFetched;
  private static Integer numTotalPosts = new Integer(0);
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length > 0) {
      imageDir = args[0];
    }
    if (args.length > 1) {
      postIdsFilename = args[1];
    } else {
      postIdsFetched = new HashSet<Long>();
    }
    if (imageDir == null) {
      imageDir = new File(".").getAbsolutePath();
    }
    
    service = new ServiceBuilder().provider(TumblrApi.class).apiKey(TUMBLR_API_KEY).apiSecret(
        TUMBLR_API_SECRET).build();
    Token accessToken = service.getXAuthAccessToken("", "");
    System.out.println("GOT ACCESS TOKEN: " + accessToken.getToken());
    PhotoBlogDownloader.accessToken = accessToken;
    fetcher = DataFetcher.createDataFetcher();
    
    populatePostIds();
    
    PhotoBlogDownloader downloader = new PhotoBlogDownloader();
    downloader.fetchPosts();
  }
  
  private static void populatePostIds() {
    postIdsFetched = new HashSet<Long>();
    if (postIdsFilename != null) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(postIdsFilename));
        String line;
        while ((line = br.readLine()) != null) {
          Long postId = Long.parseLong(line);
          postIdsFetched.add(postId);
          numTotalPosts++;
        }
        br.close();
      } catch (IOException e) {
        // do nothing if we can't read the file; the offset will just remain 0
      }
    }
  }
  
  private void fetchPosts() {
    int offset = 0;
    int numPosts = 0;
    
    int totalNumPosts = 0;
    OAuthRequest infoReq = new OAuthRequest(Verb.GET, TumblrConstants.INFO_POST_URL);
    infoReq.addQuerystringParameter(TumblrConstants.API_KEY, TUMBLR_API_KEY);
    service.signRequest(accessToken, infoReq);
    Response infoResponse = infoReq.send();
    String infoResp = infoResponse.getBody();
    JSONObject jsonData = null;
    JSONObject responseJson = null;
    try {
      jsonData = new JSONObject(infoResp);
      responseJson = jsonData.getJSONObject(TumblrConstants.RESPONSE);
      System.out.println("RESPONSE JSON: " + responseJson.toString());
      JSONObject blog = responseJson.getJSONObject(TumblrConstants.BLOG);
      totalNumPosts = blog.getInt(TumblrConstants.POSTS);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    if (totalNumPosts == numTotalPosts) {
      // if there are no new posts, return;
      return;
    } else {
      numTotalPosts = totalNumPosts;
    }
    
    do {
      OAuthRequest req = new OAuthRequest(Verb.GET, TumblrConstants.API_POSTS_URL);
      req.addQuerystringParameter(TumblrConstants.API_KEY, TUMBLR_API_KEY);
      req.addQuerystringParameter(TumblrConstants.OFFSET, new Integer(offset).toString());
      req.addQuerystringParameter(TumblrConstants.LIMIT, new Integer(FETCH_LIMIT).toString());
      service.signRequest(accessToken, req);
      Response response = req.send();
      String resp = response.getBody();
      System.out.println("GOT BLOG POSTS: ");
      System.out.println(resp);
      try {
        jsonData = new JSONObject(resp);
        responseJson = jsonData.getJSONObject(TumblrConstants.RESPONSE);
        System.out.println("RESPONSE JSON: " + responseJson.toString());
        JSONArray posts = responseJson.getJSONArray(TumblrConstants.POSTS);
        saveImages(posts, offset);
        numPosts = posts.length();
        offset = offset + numPosts;
      } catch (JSONException e) {
        e.printStackTrace();
      }
    } while (numPosts > 0);
    
    persistLatestPostIds();
  }
  
  private void saveImages(JSONArray posts, int offset) {
    for (int i = 0; i < posts.length(); i++) {
      try {
        JSONObject post = posts.getJSONObject(i);
        long id = post.getLong(TumblrConstants.ID);
        if (postIdsFetched.contains(id)) {
          System.out.println("SKIPPING " + id + " because already downloaded");
          continue;
        }
        String postType = post.getString(TumblrConstants.TYPE);
        String date = post.getString(TumblrConstants.DATE);
        int type = TumblrConstants.getType(postType);
        if (type == TumblrConstants.PHOTO_TYPE) {
          JSONArray photos = post.getJSONArray(TumblrConstants.PHOTOS);
          for (int j = 0; j < photos.length(); j++) {
            JSONObject photo = photos.getJSONObject(j);
            JSONArray altSizes = photo.getJSONArray(TumblrConstants.ALT_SIZES);
            String largestUrl = "";
            String secondLargestUrl = "";
            int width = 0;
            int secondLargestWidth = 0;
            for (int k = 0; k < altSizes.length(); k++) {
              JSONObject photoSize = altSizes.getJSONObject(k);
              int photoWidth = photoSize.getInt(TumblrConstants.WIDTH);
              if (photoWidth > width) {
                width = photoWidth;
                largestUrl = photoSize.getString(TumblrConstants.URL);
              }
              if (photoWidth < width && photoWidth > secondLargestWidth) {
                secondLargestWidth = photoWidth;
                secondLargestUrl = photoSize.getString(TumblrConstants.URL);
              }
            }
            String persistUrl = largestUrl;
            DataResponse resp = fetcher.fetch(largestUrl);
            if (resp.success == false || resp.rawData == null || resp.rawData.length < 1024 * 2) {
              resp = fetcher.fetch(secondLargestUrl);
              persistUrl = secondLargestUrl;
              
              // null out the data if the response isn't successful or if the response isn't large
              // enough to be a real image
              if (resp.success == false || resp.rawData == null || resp.rawData.length < 1024 * 2) {
                resp.rawData = null;
              }
            }
            if (resp.rawData != null) {
              persistImage(persistUrl, resp, id, date, j);
            }
          }
        }
        postIdsFetched.add(id);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }
  
  private void persistImage(String url, DataResponse resp, long id, String date, int photo_number) {
    if (url != null && resp.success && resp.rawData != null) {
      int lastSlash = url.lastIndexOf('/');
      String imageName = url.substring(lastSlash + 1);
      
      try {
        String dateStr = date.substring(0, 19);
        String dateReplaced = dateStr.replace(" ", "_");
        String filename = imageDir + dateReplaced + "_" + id + "_" + photo_number + "_" + imageName;
        File f = new File(filename);
        if (f.exists()) {
          System.out.println("AAAAAAAAAAAA - FILE ALREADY EXISTS: " + f.getAbsolutePath());
          return;
        }
        FileOutputStream os = new FileOutputStream(f);
        BufferedOutputStream writer = new BufferedOutputStream(os);
        writer.write(resp.rawData);
        writer.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  private void persistLatestPostIds() {
    if (postIdsFilename != null) {
      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(postIdsFilename));
        for (Long id : postIdsFetched) {
          writer.write(id.toString() + "\n");
        }
        writer.flush();
        writer.close();
      } catch (IOException e) {
        // do nothing;
      }
    }
  }
}
