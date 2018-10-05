package com.example.nfs05.photogallery.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.nfs05.photogallery.R;
import com.example.nfs05.photogallery.data.PhotoItem;
import com.example.nfs05.photogallery.networks.FlickerFetcher;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment  {



    private RecyclerView mRecyclerView;
    private final String TAG = "photoFragment";
    private List<PhotoItem> mPhotoItemList = new ArrayList<>();
    private GridLayoutManager layoutManager ;
    private RelativeLayout mRelativeLayout ;
    private FetchPhotoTask task ;
    private int currentPage ;
    private FlickerFetcher mFlickerFetcher ;



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
            // TODO: 10/4/18  add boolean variable . 
            if ((firstItemPosition + visibleItemCount) >= totalItemCount){
                mRelativeLayout.setVisibility(View.VISIBLE);
//                increaseCurrentPage();
                // todo :: execute task again ..
                new FetchPhotoTask().execute(2);
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
        mFlickerFetcher = new FlickerFetcher();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery,container,false) ;
        mRecyclerView = view.findViewById(R.id.photo_recycler_view);
        mRelativeLayout = view.findViewById(R.id.relative_layout);
        mRelativeLayout.setVisibility(View.GONE);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float columnWidthInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, getActivity().getResources().getDisplayMetrics());
                int width = mRecyclerView.getWidth() ;
//                Toast.makeText(getActivity()," columnWidth = "+ columnWidthInPixels+ " Recycler Width = "+width,Toast.LENGTH_LONG).show();
                int column_number = Math.round(width / columnWidthInPixels);
                layoutManager = new GridLayoutManager(getActivity(),column_number);
                mRecyclerView.setLayoutManager(layoutManager);
                mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
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
//        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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
            return new FlickerFetcher().fetchPhotos(integers[0]);
        }

        @Override
        protected void onPostExecute(List<PhotoItem> photoItems) {
            super.onPostExecute(photoItems);
            mRelativeLayout.setVisibility(mRecyclerView.isEnabled()?View.GONE:View.VISIBLE);
            if (!mPhotoItemList.isEmpty()){
                final int oldPage = mPhotoItemList.size();
                mPhotoItemList.addAll(photoItems);
                mRecyclerView.getAdapter().notifyDataSetChanged();
                Log.i(TAG," Current size of list = "+mPhotoItemList.size());
//                mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//
//                    @Override
//                    public void onGlobalLayout() {
//                       mRecyclerView.smoothScrollToPosition(oldPage);
//                       mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    }
//                });
            }else{
                mPhotoItemList = photoItems;
                setupAdapter();
            }
        }

    }

    // load more photos.
    private void increaseCurrentPage(){
        currentPage++  ;
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        Log.i(TAG,"FirstVisibleItem "+firstVisibleItem);
    }

    /** This method how we can calculate a whole window   **/
//    private void refreshGridView(){
//        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        Display display = windowManager.getDefaultDisplay();
//        Point point = new Point();
//        display.getSize(point);
//        int width = point.x;
//        int height = point.y;
//        Toast.makeText(getActivity()," width = "+width + " height = "+height,Toast.LENGTH_SHORT).show();
//    }
    @Override
    public void onPause() {
        super.onPause();
        task.cancel(true);
    }

    private class FetchMorePageTask extends AsyncTask<Void,Void,List<PhotoItem>>{

        @Override
        protected List<PhotoItem> doInBackground(Void... voids) {
            return mFlickerFetcher.fetchPhotos(currentPage);
        }


        @Override
        protected void onPostExecute(List<PhotoItem> photoItems) {
            super.onPostExecute(photoItems);
        }
    }
}

