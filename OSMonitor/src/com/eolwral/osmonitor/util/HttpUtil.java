package com.eolwral.osmonitor.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

//
// base on
// http://cypressnorth.com/mobile-application-development/setting-android-google-volley-imageloader-networkimageview/
//

public class HttpUtil {

	private  static final String TAG = "Volley";
	
    private static HttpUtil mInstance = null;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    
    private HttpUtil(Context context){
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(this.mRequestQueue, new ImageLruCache());
    }
    
    private class  ImageLruCache implements ImageCache {
    	private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(3);
    	
        public void putBitmap(String url, Bitmap bitmap) {
            mCache.put(url, bitmap);
        }
        
        public Bitmap getBitmap(String url) {
            return mCache.get(url);
        }
    }
    
    public static HttpUtil getInstance(Context context){
    	if(mInstance == null){
            mInstance = new HttpUtil(context);
        }
        return mInstance;
    }
 
    public void addRequest(StringRequest newRequest){
    	
    	if (newRequest == null)
    		return;
    	
    	newRequest.setTag(TAG);
    	mRequestQueue.add(newRequest);
        return;
    }

    public void addRequest(JsonRequest<?> newRequest){
    	
    	if (newRequest == null)
    		return;
    	
    	newRequest.setTag(TAG);
    	mRequestQueue.add(newRequest);
        return;
    }
    
   public void addRequest(WHOISRequest newRequest){
    	
    	if (newRequest == null)
    		return;
    	
    	newRequest.setTag(TAG);
    	mRequestQueue.add(newRequest);
        return;
    }

    public void cancelRequest() {
    	mRequestQueue.cancelAll(TAG);
    }
 
    public ImageLoader getImageLoader(){
        return this.mImageLoader;
    }
 
}