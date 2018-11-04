package com.example.nfs05.photogallery.service;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PollServiceTwo extends JobService {


    private static  String TAG = "MyTest";
    private final static int JOB_ID = 001 ;

    private static JobScheduler mJobScheduler ;
    private static JobInfo mJobInfo ;

    public  Context mContext ;

    GetLastImageTask mGetLastImageTask ;

    public PollServiceTwo(Context context){
        mContext = context ;
        Log.d(TAG,"Start Service class ");
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        Log.d(TAG,"onStartJob call ");
//        mGetLastImageTask = new GetLastImageTask(){
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                Log.d(TAG," Execute done.");
//                jobFinished(jobParameters,false);
//            }
//        };
//        mGetLastImageTask.execute();
        return true; // set true because i use asyncTask .
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        mGetLastImageTask.cancel(true);
        return false;
    }


    private static class GetLastImageTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 10 ; i > 0 ; i--){
                Log.d(TAG," Running : "+i);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public  void startSchulder(Context context){
        JobInfo jobInfo =
                new JobInfo.Builder(JOB_ID,
                        new ComponentName(context,PollServiceTwo.class))
                .setPeriodic(500)
                .setPersisted(true)
                .build();


//        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,new ComponentName(mContext,PollServiceTwo.class));
//            builder.setPeriodic(500)
//                    .setPersisted(true);
//
//            mJobInfo = builder.build();
            mJobScheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
            mJobScheduler.schedule(jobInfo);

    }

    private static void stopSchulder(){

        mJobScheduler.cancel(JOB_ID);
    }

    // call this method from outside class .
    public  void startService(Context context ,int status ){
         mContext = context ;
//        new PollServiceTwo(context);
       if (status == 1){
           startSchulder(mContext);
       }else{
           stopSchulder();
       }
    }

}
