package com.example.nfs05.photogallery.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.nfs05.photogallery.PhotoGalleryActivity;
import com.example.nfs05.photogallery.R;
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

        // Get a default Shared Preferences .
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
       // Start notification
        Resources resources = getResources(); // this for get title
        Intent i = PhotoGalleryActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this,"PhotoGalleryChannel");

                notification.setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pi)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.notify(0,notification.build());
       QueryPreferences.setLastResultId(this,resultId);
    }

    // If PendingIntent Not exists , create .
    public static boolean isServiceAlarmOn(Context context){
       Intent i = PollService.newIntent(context);
       PendingIntent pi = PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return pi != null ; // if pi return null that means Alarm not set .
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
