package com.example.nfs05.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.nfs05.photogallery.fragments.PhotoGalleryFragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    // run activity from notification ..
    public static Intent newIntent(Context context){
        return new Intent(context,PhotoGalleryActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fragment_single;
    }
}
