package com.timappweb.timapp.fragments;

import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.databinding.FragmentExploreMapBinding;
import com.timappweb.timapp.listeners.FabListenerFactory;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.map.EventClusterRenderer;
import com.timappweb.timapp.map.MapFactory;
import com.timappweb.timapp.map.RemovableNonHierarchicalDistanceBasedAlgorithm;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestItemFactory;
import com.timappweb.timapp.utils.AreaDataCaching.OnDataChangeListener;
import com.timappweb.timapp.utils.AreaDataCaching.RAMAreaRequestItem;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.MyLocationProvider;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.SimpleTimerView;

import java.util.List;

public class ExploreMapFragment extends Fragment implements LocationManager.LocationListener, OnMapReadyCallback {

    private static final String             TAG                             = "ExploreMapFragment";
    private static final int                MARGIN_BUTTON_LOCATE_MAP        = 120;
    private static final int                PRECISION_LAT_LONG_MAP          = 5 ;
    private static final int                TIME_ZOOM_ANIM                  = 500;
    private static final int                DELAY_UPDATE_MAP_DATA_MILLIS    = 500;
    private static final double             PADDING_RATIO_ZOOM_CLUSTER = 0.2;
    private static final float              GO_TO_LIST_ZOOM_LEVEL = 2;
    private float                           ZOOM_LEVEL_CENTER_MAP           = 15.0f;
    private Marker                          selectingMarker;
    private Location                        lastLocation;

    // ---------------------------------------------------------------------------------------------


    // Declare a variable for the cluster manager.
    private ClusterManager<Event>           mClusterManagerPost;
    private GoogleMap                       gMap = null;
    private MapView                         mapView = null;

    private View                            root;
    private View                            eventView;
    private View                            progressView;
    private HorizontalTagsRecyclerView      filterTagsRv;
    private View                            filterTagsContainer;
    private SimpleTimerView                 tvCountPoints;
    private View                            btnAddEvent;
    private View gpsCenterButton;
    private View                            cameraButton;
    private View                            tagButton;
    private View                            inviteButton;
    private View                            comingButton;

    private ExploreFragment                 exploreFragment;
    private FragmentExploreMapBinding       mBinding;
    private AreaRequestHistory              history;
    private Handler                         mMainHandler;
    private Runnable                        mUpdateRunnable;

    private boolean                         needCenterMap           = true;
    private OnZoomChangeListener mOnCameraMoveListener;
    // ---------------------------------------------------------------------------------------------

    public AreaRequestHistory getHistory() {
        return history;
    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        this.lastLocation = lastLocation;
        if (needCenterMap){
            centerMap(newLocation);
        }
    }

    // ---------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_explore_map, container, false);
        root = mBinding.getRoot();
        exploreFragment = (ExploreFragment) getParentFragment();
        setHasOptionsMenu(true);
        mapView = (MapView) root.findViewById(R.id.map);
        mapView.onCreate(null);
        progressView = root.findViewById(R.id.progress_view);
        filterTagsRv = (HorizontalTagsRecyclerView) root.findViewById(R.id.search_tags);
        filterTagsContainer = root.findViewById(R.id.search_tags_container);
        tvCountPoints = (SimpleTimerView) root.findViewById(R.id.points_text);
        btnAddEvent = root.findViewById(R.id.fab_button_add_event);
        gpsCenterButton = root.findViewById(R.id.fab_button_location);
        cameraButton = root.findViewById(R.id.action_camera);
        tagButton = root.findViewById(R.id.action_tag);
        inviteButton = root.findViewById(R.id.action_invite);
        comingButton = root.findViewById(R.id.action_coming);

        eventView = root.findViewById(R.id.event_view_layout_map);
        eventView.setVisibility(View.GONE);

        mMainHandler = new Handler(getContext().getMainLooper());

        initListeners();

        return root;
    }

    private void initListeners() {

        eventView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.viewSpecifiedEvent(getActivity(), mBinding.getEvent());
            }
        });

        filterTagsRv.getAdapter().setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.filter(getActivity());
            }
        });

        btnAddEvent.setOnClickListener(exploreFragment.getFabClickListener());

        gpsCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastLocation!=null && gMap != null) {
                    LatLng currentPosition = MyLocationProvider.convert(lastLocation);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(currentPosition)      // Sets latitude and longitude
                            .zoom(ZOOM_LEVEL_CENTER_MAP)  // Sets the zoom
                            .bearing(0)                   // Initialize the orientation of the camera
                            .tilt(0)                      // Initialize tilt of the camera
                            .build();                     // Creates a CameraPosition from the simpleMessage
                    gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
    }

    @Override
    public void onPause() {
        mapView.onPause();
        LocationManager.removeLocationListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (gMap == null){
            mapView.getMapAsync(this);
        }
        updateFilterView();
        LocationManager.addOnLocationChangedListener(this);
    }

    // ---------------------------------------------------------------------------------------------

    public void setLoaderVisibility(boolean bool) {
        progressView.setVisibility(bool ? View.VISIBLE : View.GONE);
    }

    private void displayEvent(Event event) {
        eventView.setVisibility(View.VISIBLE);
        mBinding.setEvent(event);

        FabListenerFactory.setFabListener(getContext(), root, event);

        final Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_up_alpha);
        eventView.startAnimation(slideIn);
        Log.v(TAG, "Bottom Card Height: " + Integer.toString(eventView.getHeight()));
        Animation translateUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_up_strong);
        btnAddEvent.startAnimation(translateUp);
    }

    public void hideEvent() {
        if(eventView.getVisibility()==View.VISIBLE) {
            Log.d(TAG, "Hide event, Bottom Card Height: " + Integer.toString(eventView.getHeight()));
            removeCurrentMarker();
            TranslateAnimation translateDown = new TranslateAnimation(0,0,0,eventView.getHeight()+500);
            translateDown.setDuration(getResources().getInteger(R.integer.time_slide_out_map));
            translateDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    eventView.setVisibility(View.GONE);
                    Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_up);
                    btnAddEvent.startAnimation(slideIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            translateDown.setInterpolator(new DecelerateInterpolator());

            btnAddEvent.startAnimation(translateDown);
            gpsCenterButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.appear));

            Animation slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_down);
            eventView.startAnimation(slideOut);

            selectingMarker = null;
        }
    }

    public void updateFilterView() {
        if(isFilterActive()) {
            filterTagsContainer.setVisibility(View.VISIBLE);
            filterTagsRv.getAdapter().setData(MyApplication.searchFilter.tags);
            Log.d(TAG,"Number of tags filtered : " + MyApplication.searchFilter.tags.size());
            //mapView.getMap().setPadding(0, MARGIN_BUTTON_LOCATE_MAP, 0, 0);
        } else {
            filterTagsContainer.setVisibility(View.GONE);
            //mapView.getMap().setPadding(0, 0, 0, 0);
        }
        getActivity().invalidateOptionsMenu();
    }

    public boolean isFilterActive() {
        return MyApplication.searchFilter.tags!=null && MyApplication.searchFilter.tags.size()!=0;
    }

    public GoogleMap getMap(){
        return this.gMap;
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is now ready!");
        initMap(googleMap);
        setUpClusterer();
        initAreaRequestHistory();
        exploreFragment.getDataLoader().setAreaRequestHistory(this.history);

        if (LocationManager.hasLastLocation()){
            centerMap(LocationManager.getLastLocation());
        }
    }

    private void initMap(GoogleMap googleMap) {
        gMap = googleMap;
        MapFactory.initMap(gMap, true);
        gMap.getUiSettings().setMapToolbarEnabled(false);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);


        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideEvent();
            }
        });
    }


    private void initAreaRequestHistory(){
        history = new AreaRequestHistory(exploreFragment.getDataLoader());
        history.setAreaRequestItemFactory(new AreaRequestItemFactory() {
            @Override
            public RAMAreaRequestItem build() {
                final RAMAreaRequestItem<Event> requestItem = new RAMAreaRequestItem<Event>();
                requestItem.setListener(new OnDataChangeListener() {
                    @Override
                    public void onDataChange() {
                        Log.d(TAG, "RAMAreaRequestItem.onDataChange(): ");
                        updateMapDisplay();
                    }
                });
                return requestItem;
            }
        });
    }

    private void centerMap(Location location){
        centerMap(location, null);
    }
    private void centerMap(Location location,  GoogleMap.CancelableCallback  callback){
        if (gMap != null){
            needCenterMap = false;
            centerMap(MyLocationProvider.convert(location), callback);
        }
    }
    public void centerMap(LatLng latLng, GoogleMap.CancelableCallback  callback){
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL_CENTER_MAP), callback);
    }

    /**
     * @warning: bounds can be null
     * @return
     */
    public LatLngBounds getMapBounds(){
        if (gMap == null) return null;
        /*
        LatLngBounds cpyBounds;
        LatLngBounds bounds = mapView.getMap().getProjection().getVisibleRegion().latLngBounds;
        synchronized (bounds){
            cpyBounds = new LatLngBounds(bounds.southwest, bounds.northeast);
        }*/
        return  gMap.getProjection().getVisibleRegion().latLngBounds;
    }

    public void updateMapData(){
        if (mUpdateRunnable != null){
            mMainHandler.removeCallbacks(mUpdateRunnable);
            mUpdateRunnable = null;
        }
        final LatLngBounds bounds = getMapBounds();
        if (bounds == null) return;
        Log.d(TAG, "Map bounds: " + bounds.southwest + " " + bounds.southwest);

        if (!history.isInitialized() || (mOnCameraMoveListener != null
                && mOnCameraMoveListener.isZoomMode(OnZoomChangeListener.ZoomType.OUT))){
            // Remove previous cache and all markers
            history.resizeArea(bounds);
        }
        history.update(bounds);
        updateMapDisplay();
    }

    private void updateMapDisplay() {
        LatLngBounds bounds = getMapBounds();
        if (bounds != null){
            List<Event> events = history.getInsideBoundsItems(getMapBounds());
            mClusterManagerPost.clearItems();
            mClusterManagerPost.addItems(events);
            mClusterManagerPost.cluster();
        }
    }

    private void setUpClusterer(){
        Log.i(TAG, "Setting up cluster!");
        if (gMap == null){
            throw new InternalError("Trying to @setUpClusterer() but gMap is NULL");
        }

        // Initialize the manager with the context and the map.
        mClusterManagerPost = new ClusterManager<>(getActivity(), gMap);
        mClusterManagerPost.setRenderer(new EventClusterRenderer(getActivity(), gMap, mClusterManagerPost));
        mClusterManagerPost.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Event>() {
            @Override
            public boolean onClusterClick(Cluster<Event> cluster) {

                // If zoom level is too big, go to list
                if (gMap.getCameraPosition().zoom > gMap.getMaxZoomLevel() - GO_TO_LIST_ZOOM_LEVEL){
                    exploreFragment.actionList();
                    return true;
                }

                Log.d(TAG, "You clicked on a cluster");
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                //simpleMessage.include(cluster.getPosition());
                for (Event m : cluster.getItems()) {
                    builder.include(m.getPosition());
                }
                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * PADDING_RATIO_ZOOM_CLUSTER); // offset from edges of the map 12% of screen
                Log.d(TAG, "PADDING FOR ZOOM IN MAP CLUSTER = " + padding + " meters");
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                gMap.animateCamera(cameraUpdate);

                hideEvent();
                return true;
            }
        });
        mClusterManagerPost.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Event>() {
            @Override
            public boolean onClusterItemClick(Event event) {
                Log.d(TAG, "You clicked on a cluster item: " + event);
                if(!(isPlaceViewVisible() && mBinding.getEvent() == event)) {
                    selectUI(event);
                }
                return false;
            }
        });
        //If we want to redirect to the place after second click, please set another listener  to the map
        // new OnMarkerClickListener() and return false to cancel center on map.
        gMap.setOnMarkerClickListener(mClusterManagerPost);

        mOnCameraMoveListener = new OnZoomChangeListener(gMap){
            @Override
            public void onCameraMove() {
                super.onCameraMove();
                mClusterManagerPost.cluster();

                if (mUpdateRunnable != null){
                    mMainHandler.removeCallbacks(mUpdateRunnable);
                }
                mUpdateRunnable = new Runnable() {
                    @Override
                    public void run() {
                        ExploreMapFragment.this.updateMapData();
                    }
                };
                mMainHandler.postDelayed(mUpdateRunnable, DELAY_UPDATE_MAP_DATA_MILLIS);
            }
        };
        gMap.setOnCameraMoveListener(mOnCameraMoveListener);
        mClusterManagerPost.setAlgorithm(new RemovableNonHierarchicalDistanceBasedAlgorithm<Event>());

        this.exploreFragment.getDataLoader().setClusterManager(mClusterManagerPost);
    }

    // ---------------------------------------------------------------------------------------------

    private void selectUI(Event event){
        displayEvent(event);

        removeCurrentMarker();
        MarkerOptions markerOptions = event.getMarkerOption();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(getResources().getIdentifier("marker_secondary","drawable", getContext().getPackageName())));
        markerOptions.anchor(0.5f,0.5f); //center Marker Bitmap
        if (gMap != null)  selectingMarker = gMap.addMarker(markerOptions);
    }

    public Marker getSelectingMarker() {
        return selectingMarker;
    }

    private void removeCurrentMarker() {
        if(selectingMarker!= null) selectingMarker.remove();
    }

    public boolean isPlaceViewVisible() {
        return eventView.getVisibility()==View.VISIBLE;
    }

    private abstract static class OnZoomChangeListener implements GoogleMap.OnCameraMoveListener{

        private final GoogleMap gMap;

        public OnZoomChangeListener(GoogleMap gMap) {
            this.currentZoomMode = ZoomType.NONE;
            this.gMap = gMap;
        }

        enum ZoomType {IN, OUT, NONE}

        private ZoomType                        currentZoomMode                 = ZoomType.NONE;
        private float                           previousZoomLevel = -1;
        private float                           currentZoomLevel = -1;



        public boolean isZoomMode(ZoomType mode) {
            return this.currentZoomMode == mode;
        }

        // TODO
        public float getZoomDiffRatio(){
            return Math.abs((this.previousZoomLevel - this.currentZoomLevel) / this.previousZoomLevel);
        }

        private void computeZoom(){
            CameraPosition cameraPosition = this.gMap.getCameraPosition();
            Log.d(TAG, "Zoom level is now: " + cameraPosition.zoom);

            previousZoomLevel = currentZoomLevel;
            currentZoomLevel = cameraPosition.zoom;
            // Register zoom updates
            if (previousZoomLevel == currentZoomLevel) {
                currentZoomMode = ZoomType.NONE;
            } else if (previousZoomLevel > currentZoomLevel) {
                currentZoomMode = ZoomType.OUT;
            } else {
                currentZoomMode = ZoomType.IN;
            }

            previousZoomLevel = cameraPosition.zoom;
        }

        @Override
        public void onCameraMove() {
            this.computeZoom();
        }
    }
}
