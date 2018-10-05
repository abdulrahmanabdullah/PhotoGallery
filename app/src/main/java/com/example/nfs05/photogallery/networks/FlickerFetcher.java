package com.example.nfs05.photogallery.networks;

import android.net.Uri;
import android.util.Log;

import com.example.nfs05.photogallery.data.PhotoItem;
import com.example.nfs05.photogallery.data.Photos;
import com.example.nfs05.photogallery.data.Response;
import com.example.nfs05.photogallery.model.GalleryItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickerFetcher {

    private Response mResponse ;
  private  Photos mPhotos ;
  private PhotoItem mPhotoItem ;
    public FlickerFetcher(){
        mResponse = new Response();
        mPhotos = new Photos();
        mPhotoItem = new PhotoItem();
    }
    private String TAG = "Fetcher";
    private String API_KEY = "cd323fa75ca30d9f80b5525bb3f1ac09";
    // open connect and get bytes stream .
    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();
            // check url is correct .If not throw this exception .
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+ " :with "+urlSpec);
            }
            int bytesRead = 0 ;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer))> 0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return  out.toByteArray();
        }finally {
           connection.disconnect();
        }
    }



    // convert bytes stream to string .
    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }


    public List<PhotoItem> fetchPhotos(int page){
        List<PhotoItem> list = new ArrayList<>();
        String url = Uri.parse("https://api.flickr.com/services/rest/")
                .buildUpon()
                .appendQueryParameter("method","flickr.photos.getRecent")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("format","json")
                .appendQueryParameter("nojsoncallback","1")
                .appendQueryParameter("extras","url_s")
                .appendQueryParameter("page",String.valueOf(page))
                .build()
                .toString();
           Log.i(TAG," Url now is :  " + url) ; // it's work .
        try {
            String jsonString = getUrlString(url);
            list = parseItemWithGson(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list ;
    }
    public int getPage(){
        // return current page,from Photo class .
        return  mPhotos.getPage() ;
    }
    private List<PhotoItem> parseItemWithGson(String jsonString){
        mResponse = new Gson().fromJson(jsonString, Response.class);
        mPhotos = mResponse.getPhotos();
        List<PhotoItem> photoItem = mPhotos.getPhoto();
//        Log.i(" test "," Here size of List " + photoItem.size());
        return photoItem ;
    }

    // get json object ..
    private void parseItems(List<GalleryItem> items , JSONObject jsonObject) throws IOException , JSONException {
        JSONObject index = jsonObject.getJSONObject("photos");
        JSONArray photoJsonArray = index.getJSONArray("photo");

        for (int i = 0 ; i < photoJsonArray.length();i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")){
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }

    }
}
