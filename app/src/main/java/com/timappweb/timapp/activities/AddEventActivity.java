package com.timappweb.timapp.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.AddEventCategoriesAdapter;
import com.timappweb.timapp.adapters.EventCategoryPagerAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.databinding.ActivityAddEventBinding;
import com.timappweb.timapp.managers.SpanningGridLayoutManager;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.rest.model.RestValidationErrors;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.BackCatchEditText;

import retrofit2.Call;


public class AddEventActivity extends BaseActivity{
    private static final float ZOOM_LEVEL_CENTER_MAP = 12.0f;
    private String TAG = "AddEventActivity";

    private InputMethodManager imm;

    //Views
    private BackCatchEditText eventNameET;
    RecyclerView categoriesRV;
    AddEventCategoriesAdapter categoriesAdapter;
    private EventCategory eventCategorySelected;
    private View createButton;
    private View progressView;
    private TextView nameCategoryTV;
    //private View pinView;
    private ViewPager viewPager;
    //private SpotView spotView;
    // Data
    private Spot spot = null;

    private MapView mapView = null;
    private GoogleMap gMap;
    private ActivityAddEventBinding mBinding;
    private View mButtonAddPicture;
    private View mBtnAddSpot;
    //private View eventLocation;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_event);

        this.initToolbar(true);
        this.extractSpot(savedInstanceState);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        eventNameET = (BackCatchEditText) findViewById(R.id.event_name);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(ConfigurationProvider.rules().places_max_name_length);
        eventNameET.setFilters(filters);
        eventNameET.requestFocus();

        categoriesRV = (RecyclerView) findViewById(R.id.rv_categories);
        createButton = findViewById(R.id.create_button);
        progressView = findViewById(R.id.progress_view);
        nameCategoryTV = (TextView) findViewById(R.id.category_name);
        mapView = (MapView) findViewById(R.id.map);
        mButtonAddPicture = findViewById(R.id.button_take_picture);
        mBtnAddSpot = (Button) findViewById(R.id.button_add_spot);

        Event event = new Event();
        if (LocationManager.hasLastLocation()){
            event.setLocation(LocationManager.getLastLocation());
        }
        mBinding.setEvent(event);

        initKeyboard();
        setListeners();
        initAdapterAndManager();
        initViewPager();
        initLocationListener();
        setButtonValidation();
        initEvents();
        initMap();
    }

    private void initEvents() {
        mButtonAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPicture(AddEventActivity.this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
    //----------------------------------------------------------------------------------------------
    //Private methods

    /**
     * Load places once user name is known
     */
    private void initLocationListener() {
        LocationManager.addOnLocationChangedListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationChanged(Location newLocation, Location lastLocation) {
                // TODO
                Log.v(TAG, "User location changed!");
                updateMapCenter(newLocation);
            }
        });
        LocationManager.start(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initKeyboard() {
        eventNameET.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void initAdapterAndManager() {
        categoriesAdapter = new AddEventCategoriesAdapter(this);
        categoriesRV.setAdapter(categoriesAdapter);
        GridLayoutManager manager = new SpanningGridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        categoriesRV.setLayoutManager(manager);
    }

    private void setProgressView(boolean bool) {
        if(bool) {
            progressView.setVisibility(View.VISIBLE);
            getSupportActionBar().hide();
        }
        else {
            progressView.setVisibility(View.GONE);
            getSupportActionBar().show();
        }
    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.addplace_viewpager);
        final EventCategoryPagerAdapter eventCategoryPagerAdapter = new EventCategoryPagerAdapter(this);
        viewPager.setAdapter(eventCategoryPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        eventCategorySelected = categoriesAdapter.getCategory(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                EventCategory newEventCategory = categoriesAdapter.getCategory(position);
                categoriesAdapter.setIconNewCategory(AddEventActivity.this, newEventCategory);
                eventCategorySelected = newEventCategory;
            }
        });
    }

    private void submitPlace(final Event event){
        Log.d(TAG, "Submit event " + event.toString());
        Call call = RestClient.service().addPlace(event);
        event.saveRemoteEntry(this, call, new RestFeedbackCallback(this){

            @Override
            public void onActionSuccess(RestFeedback feedback) {
                IntentsUtils.viewEventFromId(AddEventActivity.this, event.remote_id);
            }

            @Override
            public void onActionFail(RestFeedback feedback) {
                if (feedback == null) return;;
                RestValidationErrors errors = feedback.getValidationErrors();
                mBinding.setErrors(errors);
            }

            @Override
            public void onFinish() {
                setProgressView(false);
            }
        });

    }

    public void setButtonValidation() {
        String textAfterChange = eventNameET.getText().toString().trim();
        createButton.setEnabled(eventCategorySelected != null && Event.isValidName(textAfterChange));
    }

    //----------------------------------------------------------------------------------------------
    //Public methods
    public RecyclerView getCategoriesRV() {
        return categoriesRV;
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    // ----------------------------------------------------------------------------------------------
    //Inner classes

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public void setCategory(EventCategory eventCategory) {
        eventCategorySelected = eventCategory;
    }

    public EventCategory getEventCategorySelected() {
        return eventCategorySelected;
    }

    private void setListeners() {
        mBtnAddSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.pinSpot(AddEventActivity.this);
            }
        });

        eventNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setButtonValidation();
            }
        });

        eventNameET.setHandleDismissingKeyboard(new BackCatchEditText.HandleDismissingKeyboard() {
            @Override
            public void dismissKeyboard() {
                imm.hideSoftInputFromWindow(eventNameET.getWindowToken(), 0);   //Hide keyboard
                eventNameET.clearFocus();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocationManager.hasFineLocation(ConfigurationProvider.rules().gps_min_accuracy_add_place)) {
                    setProgressView(true);
                    Event event = mBinding.getEvent();
                    event.setCategory(eventCategorySelected);
                    event.setSpot(AddEventActivity.this.spot);
                    submitPlace(event);
                } else if (LocationManager.hasLastLocation()) {
                    Toast.makeText(getBaseContext(), "We don't have a fine location. Make sure your gps is enabled.", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Click on add event before having a user location");
                    Toast.makeText(getBaseContext(), R.string.please_wait_location, Toast.LENGTH_LONG).show();
                }
            }
        });
/*
        spotView.setOnSpotClickListener(new OnSpotClickListener() {
            @Override
            public void onEditClick() {
                IntentsUtils.pinSpot(context);
            }

            @Override
            public void onRemoveClick() {
                spot = null;
                spotView.setVisibility(View.GONE);
                //pinView.setVisibility(View.VISIBLE);
            }
        });*/
    }

    private void extractSpot(Bundle bundle){
        if(bundle!=null) {
            spot = (Spot) bundle.getSerializable("spot");
            if (spot != null){
                Log.v(TAG, "Spot is selected: " + spot);
                //spotView.setSpot(spot);
                //spotView.setVisibility(View.VISIBLE);
                //pinView.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "spot is null");
            }
        }
    }


    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        Log.d(TAG, "ExploreMapFragment.onResume()");
        this.loadMapIfNeeded();
    }

    private void loadMapIfNeeded() {
        try {
            if (gMap == null){
                gMap = mapView.getMap();
            }
            gMap.setIndoorEnabled(true);
            Location location = LocationManager.getLastLocation();
            if (location != null){
                updateMapCenter(location);
                //gMap.addMarker(event.getMarkerOption());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMapCenter(Location location){
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL_CENTER_MAP));

    }

    private void initMap(){
        mapView.onCreate(null);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "Map is now ready!");
                gMap = googleMap;
            }
        });
        gMap = mapView.getMap();
        gMap.setIndoorEnabled(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        eventNameET.clearFocus();
        switch (requestCode){
            case IntentsUtils.REQUEST_PICK_SPOT:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "extracting bundle Spot");
                    extractSpot(data.getExtras());
                }
                break;
            case IntentsUtils.ACTION_ADD_EVENT_PICTURE:
                if(resultCode==RESULT_OK) {
                    // TODO
                    Log.d(TAG, "Result OK from TagActivity");
                }
                break;
            default:
                Log.e(TAG, "Unknown activity result: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}