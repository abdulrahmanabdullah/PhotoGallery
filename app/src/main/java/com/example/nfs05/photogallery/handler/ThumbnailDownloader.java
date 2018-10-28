package com.example.nfs05.photogallery.handler;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import com.example.nfs05.photogallery.networks.FlickerFetcher;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private  static final String TAG  ="ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0 ; // this is what message .

    private boolean mHasQuit = false ;
    private Handler mRequestHandler ; // This is responsible for queue download request .
    private ConcurrentHashMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler ;
    private ThumbnailDownloaderListener<T> mDownloaderListener;
    private int availableMemByte;
    public LruCache<String,Bitmap> bitmapCache;
    private Context mContext;

    public interface ThumbnailDownloaderListener<T>{
        void onThumbnailDownloaded(T target ,Bitmap bitmap);
    }

    public void setDownloaderListener(ThumbnailDownloaderListener<T> downloaderListener) {
        mDownloaderListener = downloaderListener;
    }

    public ThumbnailDownloader(Context context ,Handler handler) {
        super(TAG);
        mContext = context;
        mResponseHandler = handler;
        ActivityManager co =(ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        availableMemByte = co.getMemoryClass()*1024*1024;
        bitmapCache = new LruCache<>(availableMemByte /8);
    }

    @Override
    public boolean quit() {
        mHasQuit = true ;
        return super.quit();
    }

    public void queueThumbnail(T target , String url){
        Log.i(TAG," Got a URL : "+url );

        if (url == null){
            mRequestMap.remove(target);
        }else{
            mRequestMap.put(target,url);//update request map .
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target) // post a new message to the background thread .
                    .sendToTarget();
        }
    }


    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD){
                    T target = (T)msg.obj;
                    Log.i(TAG," Got a request for URL "+mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    // Called when download message is pulled and ready to processed.
    private void handleRequest(final T target){
        try{
           final String url = mRequestMap.get(target);
           if (url == null) return;
           byte[] bitmapBytes = new FlickerFetcher().getUrlBytes(url);
           final Bitmap bitmap = BitmapFactory
                   .decodeByteArray(bitmapBytes,0,bitmapBytes.length);
           /*** */
            bitmapCache.put(url,bitmap);
            Log.i(TAG," xBitmap created ");
//           Log.i(TAG," Bitmap created");
//            bitmapCache.put(url,bitmap);
           mResponseHandler.post(new Runnable() {
               @Override
               public void run() {
                if (mRequestMap.get(target) != url || mHasQuit){
                   return;
                }
                mRequestMap.remove(target);
                mDownloaderListener.onThumbnailDownloaded(target,bitmap);
               }
           });
        }catch (IOException ioe){
           Log.e(TAG," Error downloading :(");
        }
    }

    // when user rotate screen .
    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }
}
