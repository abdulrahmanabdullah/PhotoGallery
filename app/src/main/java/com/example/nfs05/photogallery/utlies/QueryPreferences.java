package com.example.nfs05.photogallery.utlies;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

public class QueryPreferences {

    private static final String PREE_SEARCH_QUERY = "searchQuery";
    private static final String PREE_LAST_ID = "lastResultId";



    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREE_SEARCH_QUERY,null);
    }


    public static void setStoredQuery(Context context , String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREE_SEARCH_QUERY,query)
                .apply();
        Log.i("pho"," you search for ** " + query + " **");
    }


    // notify service for last result by id ..
    public static String getLastResultId(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREE_LAST_ID,null);
    }

    // set the latest result id .
    public static void setLastResultId(Context context , String lastResultId){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREE_LAST_ID,lastResultId)
                .apply();
    }
}
