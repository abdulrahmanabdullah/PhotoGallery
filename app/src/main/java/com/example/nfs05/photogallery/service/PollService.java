package com.example.nfs05.photogallery.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.nfs05.photogallery.data.PhotoItem;
import com.example.nfs05.photogallery.model.GalleryItem;
import com.example.nfs05.photogallery.networks.FlickerFetcher;
import com.example.nfs05.photogallery.utlies.QueryPreferences;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {

    private static final String TAG = "PollService";
    // Set interval to 1 minute .
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);
    public static Intent newIntent(Context context){
        return new Intent(context,PollService.class);
    }
    public PollService(){
        super(TAG);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // if user not have connection not do anything .
        if(!isNetworkAvailableAndConnected()) return;

        // Get a default
        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<PhotoItem> items ;
        if (query == null){
            items = new FlickerFetcher().fetchRecentPhotos();
        }else{
            items = new FlickerFetcher().searchPhotos(query);
        }
        if (items.size() == 0)return;
       String resultId = items.get(0).getId();
       if (resultId.equals(lastResultId)){
           Log.i(TAG," Got an old result : "+ resultId);
       }else{
           Log.i(TAG," Got a new result : "+resultId);
       }
       QueryPreferences.setLastResultId(this,resultId);
    }

    // Check Network available && connected
    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable =  cm.getActiveNetworkInfo()!= null ;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    public static void setServiceAlarm(Context context , boolean isOn){
        Intent intent = PollService.newIntent(context); // instance of this class .
        PendingIntent pi = PendingIntent.getService(context,0,intent,0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn){
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),POLL_INTERVAL_MS,pi);
        }else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }
}
