package com.example.nfs05.photogallery.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PollJobService extends JobService{
    private static final String TAG = "TestTwo";
    private static  Context mContext ;

    public PollJobService(Context context){
       Log.i(TAG," Service on create ");
       mContext = context;
    }
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG," Here onStart call");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i(TAG," Here onStart call");
        return false;
    }

    public static void checkApi(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
           // create jobSchedule
            createJob();
        }else{
            // Create AlarmManager .
        }
    }


    private static void createJob(){
        JobInfo jobInfo = new JobInfo.Builder(011,
                new ComponentName(mContext,PollJobService.class))
                .setPeriodic(10 * 60 * 1)
                .setPersisted(true)
                .build();
        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
    }
}
