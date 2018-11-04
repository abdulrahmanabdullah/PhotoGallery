package com.example.nfs05.photogallery.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.example.nfs05.photogallery.R;
import com.example.nfs05.photogallery.data.PhotoItem;
import com.example.nfs05.photogallery.handler.ThumbnailDownloader;
import com.example.nfs05.photogallery.networks.FlickerFetcher;
import com.example.nfs05.photogallery.service.PollJobService;
import com.example.nfs05.photogallery.service.PollService;
import com.example.nfs05.photogallery.service.PollServiceTwo;
import com.example.nfs05.photogallery.utlies.QueryPreferences;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment implements ViewTreeObserver.OnGlobalLayoutListener {



    private RecyclerView mRecyclerView;
    private final String TAG = "photoFragment";
    private List<PhotoItem> mPhotoItemList = new ArrayList<>();
    private GridLayoutManager layoutManager ;
    private RelativeLayout mRelativeLayout ;
    private final static int START_PAGE = 1 ;
    private int currentPage  = START_PAGE;
    private int total_page = 3 ;
    private boolean isLoading = false ;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    private PollJobService mJobService ;
    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
//        Intent i = PollService.newIntent(getActivity());
//        getActivity().startService(i);
//        PollService.setServiceAlarm(getActivity(),true);
        startHandler();
        checkApiLevel();
        setHasOptionsMenu(true);
//        Log.i(TAG," Background thread started .");
        updateItems();
    }

    private void startHandler(){
        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(getActivity(),responseHandler);
        mThumbnailDownloader.setDownloaderListener(new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(),bitmap);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery,menu);
        MenuItem searchItem= menu.findItem(R.id.menu_id_search);
        final SearchView searchView =(SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG," onQueryTextSubmit"+s);
                QueryPreferences.setStoredQuery(getContext(),s);
                showProgressBar(true);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG," QueryTextSubmit"+s);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query,false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else{
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // when user click clear search,set preferences as null .
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(),null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
//                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
//                PollService.setServiceAlarm(getActivity(),shouldStartAlarm);
//                getActivity().invalidateOptionsMenu();
//                startAnotherJob();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    PollJobService.checkApi();
                }
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }

    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchPhotoTask(query).execute();
   }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery,container,false) ;
        mRecyclerView = view.findViewById(R.id.photo_recycler_view);
        mRelativeLayout = view.findViewById(R.id.relative_layout);
        mRelativeLayout.setVisibility(View.GONE);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this);
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
        // avoid memory leak .
        mRecyclerView.removeOnScrollListener(onScrolled);
        mThumbnailDownloader.clearQueue();
        Log.i(TAG," Background thread destroyed ");
    }

    @Override
    public void onGlobalLayout() {
        float columnWidthInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, getActivity().getResources().getDisplayMetrics());
        int width = mRecyclerView.getWidth() ;
        int column_number = Math.round(width / columnWidthInPixels);
        layoutManager = new GridLayoutManager(getActivity(),column_number);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    // When move next page ..
    private RecyclerView.OnScrollListener onScrolled = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount() ;
            int totalItemCount  = layoutManager.getItemCount();
            int firstItemPosition  = layoutManager.findFirstVisibleItemPosition();

            if(!isLoading ){
                if ((visibleItemCount +firstItemPosition) >= totalItemCount
                        && firstItemPosition >= 0
                        && totalItemCount >= total_page
                        && currentPage <= total_page){
                    Toast.makeText(getActivity(),"Here page = "+currentPage,Toast.LENGTH_SHORT).show();
                    //todo : write some method to get more page. Done
                    loadMorePhotos();
                }
            }
        }
    };

    private void loadMorePhotos(){
        isLoading = true;
        currentPage++;
//        new FetchPhotoTask().execute();

    }

    private void showProgressBar(boolean isAppear){
         if(isAppear)  mRelativeLayout.setVisibility(View.VISIBLE) ;else mRelativeLayout.setVisibility(View.GONE);
    }
    // View Holder
    private class PhotoHolder extends RecyclerView.ViewHolder{
        private ImageView mImageView ;
        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.item_image_view);
        }
        public void bindDrawable(Drawable drawable){

            mImageView.setImageDrawable(drawable);
        }
    }

   // View adapter ..
   private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

       private List<PhotoItem> mPhotoItemList ;

       public PhotoAdapter(List<PhotoItem> photoItemList) {
           mPhotoItemList = photoItemList;
       }


       @NonNull
       @Override
       public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
           LayoutInflater inflater = LayoutInflater.from(getActivity());
           View view = inflater.inflate(R.layout.list_item_gallery,viewGroup,false);
           return new PhotoHolder(view);
       }

       @Override
       public void onBindViewHolder(@NonNull PhotoHolder holder, int i) {
           PhotoItem item = mPhotoItemList.get(i);
           String url = item.getUrlS();
           Bitmap bitmap = mThumbnailDownloader.bitmapCache.get(url == null ? "":url);
           if (bitmap == null){
               Drawable placeHolder = getResources().getDrawable(R.drawable.ic_launcher_background);
               holder.bindDrawable(placeHolder);
           }else{
              Drawable drawable = new BitmapDrawable(getResources(),bitmap);
              holder.bindDrawable(drawable);
           }
           mThumbnailDownloader.queueThumbnail(holder,item.getUrlS());

       }

       @Override
       public int getItemCount() {
           return (mPhotoItemList == null ? 0 : mPhotoItemList.size());
       }

   }

    private   class FetchPhotoTask extends AsyncTask<Void,Void,List<PhotoItem>>{
        private String mQuery ;
        public FetchPhotoTask(String query){
           mQuery = query;
        }
        @Override
        protected List<PhotoItem> doInBackground(Void... voids) {

            if (mQuery == null){
                return new FlickerFetcher().fetchRecentPhotos();
            }else{
                mPhotoItemList.clear();
                return new FlickerFetcher().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<PhotoItem> photoItems) {
            super.onPostExecute(photoItems);
            mRelativeLayout.setVisibility(mRecyclerView.isEnabled()?View.GONE:View.VISIBLE);
            if (!mPhotoItemList.isEmpty()){
                mPhotoItemList.addAll(photoItems);
                mRecyclerView.getAdapter().notifyDataSetChanged();
                isLoading = false;
            }else{
                mPhotoItemList = photoItems;
                setupAdapter();
            }
//            mPhotoItemList = photoItems;
//            setupAdapter();
        }

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
    }


    private void startAnotherJob(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            PollServiceTwo p = new PollServiceTwo(getActivity());
           p.startSchulder(getContext());
        }else{
            Toast.makeText(getActivity(),"Failed start service ",Toast.LENGTH_SHORT).show();
        }
    }

    private void checkApiLevel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
           mJobService = new PollJobService(getContext());
        }
    }

}

