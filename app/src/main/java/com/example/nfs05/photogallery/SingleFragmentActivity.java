package com.example.nfs05.photogallery;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();


    @LayoutRes
   protected int  getLayoutResId(){
       return R.layout.activity_fragment;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_fragment);
        setContentView(getLayoutResId());
        Log.i("test"," Here Activity call ");
        FragmentManager fm =getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.container_fragment);
        if (fragment == null ) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.container_fragment,fragment).commit();
        }
    }
}
