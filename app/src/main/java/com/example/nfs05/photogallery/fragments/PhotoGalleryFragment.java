package com.example.nfs05.photogallery.fragments;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nfs05.photogallery.R;
import com.example.nfs05.photogallery.data.PhotoItem;
import com.example.nfs05.photogallery.networks.FlickerFetcher;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {



    private RecyclerView mRecyclerView;
    private final String TAG = "photoFragment";
    private List<PhotoItem> mPhotoItemList = new ArrayList<>();
    private GridLayoutManager layoutManager ;
    private RelativeLayout mRelativeLayout ;
    private FetchPhotoTask task ;
    private GridLayoutManager mGridLayoutManager ;



    // When move next page ..
    private RecyclerView.OnScrollListener onScrolled = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount() ;
            int totalItemCount  = layoutManager.getItemCount();
            int firstItemPosition  = layoutManager.findFirstVisibleItemPosition();
            if ((firstItemPosition + visibleItemCount) >= totalItemCount){
                mRelativeLayout.setVisibility(View.VISIBLE);
                loadMorePhotos();
            }
        }
    };

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //start execute task .
        task = new FetchPhotoTask();
        task.execute(1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery,container,false) ;
        mRecyclerView = view.findViewById(R.id.photo_recycler_view);
        mRelativeLayout = view.findViewById(R.id.relative_layout);
        mRelativeLayout.setVisibility(View.GONE);
        mGridLayoutManager = new GridLayoutManager(getActivity(),3);
        layoutManager= mGridLayoutManager;
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        setupAdapter();
        return view;
    }

    // setup recyclerView adapter,Add current method state .
    private void setupAdapter(){
        if (isAdded()){ // Why isAdded()? Every Activities have callback , And we have worker thread in activity ,When workerThread callback check the fragment is attached .
            mRecyclerView.setAdapter(new PhotoAdapter(mPhotoItemList));
            Log.i(TAG," Photo Size = "+mPhotoItemList.size());
            mRecyclerView.addOnScrollListener(onScrolled);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        task.cancel(true);
        // avoid memory leak .
        mRecyclerView.removeOnScrollListener(onScrolled);
    }

    // View Holder
    private class PhotoHolder extends RecyclerView.ViewHolder{
        private TextView mTitleTextView ;
        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(PhotoItem item){
            mTitleTextView.setText(item.toString());
        }
    }

   // View adapter ..
   private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

//        private List<GalleryItem> mGalleryItems ;
       private List<PhotoItem> mPhotoItemList ;

       public PhotoAdapter(List<PhotoItem> photoItemList) {
           mPhotoItemList = photoItemList;
       }


       @NonNull
       @Override
       public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
           TextView textView = new TextView(getActivity());

           return new PhotoHolder(textView);
       }

       @Override
       public void onBindViewHolder(@NonNull PhotoHolder photoHolder, int i) {
//        GalleryItem item = mGalleryItems.get(i);
           PhotoItem item = mPhotoItemList.get(i);
           photoHolder.bindGalleryItem(item);
       }

       @Override
       public int getItemCount() {
           return mPhotoItemList.size();
       }
   }

    private   class FetchPhotoTask extends AsyncTask<Integer,Void,List<PhotoItem>>{

        @Override
        protected List<PhotoItem> doInBackground(Integer... integers) {
            return new FlickerFetcher().fetchPhotoItmes(integers[0]);
        }

        @Override
        protected void onPostExecute(List<PhotoItem> photoItems) {
            super.onPostExecute(photoItems);
            mRelativeLayout.setVisibility(mRecyclerView.isEnabled()?View.GONE:View.VISIBLE);
            mPhotoItemList = photoItems;
            setupAdapter();
        }

    }

    // load more photos.
    private void loadMorePhotos(){
        // TODO: 02/10/2018 solve this issue .
        if (task.getStatus() == AsyncTask.Status.FINISHED){
            new FetchPhotoTask().execute(2);
        }else{
            Toast.makeText(getActivity()," Still work ",Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshGridView(){

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshGridView();
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshGridView();
    }
}

