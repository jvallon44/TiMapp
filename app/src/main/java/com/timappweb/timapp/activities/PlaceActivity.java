package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.cache.CacheData;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.MyPagerAdapter;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.fragments.PlacePostsFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Response;

public class PlaceActivity extends BaseActivity{
    private String TAG = "PlaceActivity";
    private ListView tagsListView;
    private ListView placeListView;
    private MyPagerAdapter pagerAdapter;
    private Place place;
    private int placeId;
    private Button comingButton;
    private Button addPostButton;
    private View   progressView;
    private Activity currentActivity;

    private ShareActionProvider shareActionProvider;
    private PlacesAdapter placesAdapter;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;

        this.place = IntentsUtils.extractPlace(getIntent());
        placeId = getIntent().getIntExtra("id", -1);
        if (place == null && placeId == -1){
            IntentsUtils.home(this);
        }

        setContentView(R.layout.activity_place);
        initToolbar(true);

        //Initialize
        tagsListView = (ListView) findViewById(R.id.tags_lv);
        placeListView = (ListView) findViewById(R.id.place_lv);
        comingButton = (Button) findViewById(R.id.button_coming);
        comingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO fine location
                if (!MyApplication.hasLastLocation()){
                    Toast.makeText(getApplicationContext(), R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }
                QueryCondition conditions = new QueryCondition();
                conditions.setPlaceId(placeId);
                conditions.setAnonymous(false);
                conditions.setUserLocation(MyApplication.getLastLocation());

                Call<RestFeedback> call = RestClient.service().placeComing(conditions.toMap());
                call.enqueue(new RestFeedbackCallback(currentActivity) {
                    @Override
                    public void onActionSuccess(RestFeedback feedback) {
                        Log.d(TAG, "Success register coming for user");
                        CacheData.addUserStatus(placeId, UserPlaceStatus.COMING);
                        updateBtnVisible();
                    }

                    @Override
                    public void onActionFail(RestFeedback feedback) {
                        Log.d(TAG, "Fail register coming for user");
                    }
                });
            }
        });

        addPostButton = (Button) findViewById(R.id.button_add_some_tags);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO fine location
                if (!MyApplication.hasLastLocation()){
                    Toast.makeText(getApplicationContext(), R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }

                QueryCondition conditions = new QueryCondition();
                conditions.setPlaceId(placeId);
                conditions.setAnonymous(false);
                conditions.setUserLocation(MyApplication.getLastLocation());
                Call<RestFeedback> call = RestClient.service().placeHere(conditions.toMap());
                call.enqueue(new RestFeedbackCallback(currentActivity) {
                    @Override
                    public void onActionSuccess(RestFeedback feedback) {
                        Log.d(TAG, "Success register here for user");
                    }

                    @Override
                    public void onActionFail(RestFeedback feedback) {
                        Log.d(TAG, "Fail register here for user");
                    }
                });

                IntentsUtils.addPostStepTags(currentActivity, place);
            }
        });

        progressView = findViewById(R.id.progress_view);

        initFragments();
        initBottomButton();

        if (place != null){
            placeId = place.id;
            this.notifyPlaceLoaded();
        }
        else{
            loadPlace(placeId);
        };
    }



    private void initBottomButton() {
        if(MyApplication.hasLastLocation()) {
            double latitude =  MyApplication.getLastLocation().getLatitude();
            double longitude = MyApplication.getLastLocation().getLongitude();
        }
        // TODO steph: display the right button. ex: comingButton.setVisibility(View.VISIBLE);
    }

    private void initFragments() {
        // Création de la liste de Fragments que fera défiler le PagerAdapter
        List fragments = new Vector();

        // Ajout des Fragments dans la liste
        fragments.add(Fragment.instantiate(this, PlaceTagsFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, PlacePostsFragment.class.getName()));

        // Création de l'adapter qui s'occupera de l'affichage de la liste de
        // Fragments
        this.pagerAdapter = new MyPagerAdapter(super.getSupportFragmentManager(), fragments);

        ViewPager pager = (ViewPager) super.findViewById(R.id.place_viewpager);
        // Affectation de l'adapter au ViewPager
        pager.setAdapter(this.pagerAdapter);
    }

    private void loadPlace(int placeId) {
        final PlaceActivity that = this;
        Call<Place> call = RestClient.service().viewPlace(placeId);
        call.enqueue(new RestCallback<Place>(this) {
            @Override
            public void onResponse(Response<Place> response) {
                super.onResponse(response);
                if (response.isSuccess()){
                    Place p = response.body();
                    place = p;
                    notifyPlaceLoaded();
                }
                else{
                    Toast.makeText(that, "This place does not exists anymore", Toast.LENGTH_LONG).show();
                    IntentsUtils.home(that);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                Toast.makeText(that, "This place does not exists anymore", Toast.LENGTH_LONG).show();
                IntentsUtils.home(that);
            }

        });
    }



    private void notifyPlaceLoaded() {
        progressView.setVisibility(View.GONE);
        initPlaceAdapters();
        this.updateBtnVisible();

        // Set timer to update points for the place
        TimerTask updatePlacePoints = new TimerTask() {
            private Handler mHandler = new Handler();
            @Override
            public void run() {
                Log.d(TAG, "Running updatePlacePoints");
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // TODO place expired
                                place.points = place.points - 1;
                                placesAdapter.notifyDataSetChanged();
                                updateBtnVisible();
                            }
                        });
                    }
                }).start();
            }
        };
        timer = new Timer();
//        timer.scheduleAtFixedRate(updatePlacePoints, 1000, 1000);

        if (!MyApplication.hasLastLocation()) {
            Log.d(TAG, "There is no last known location");
            this.initLocationProvider(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    MyApplication.setLastLocation(location);
                    updateBtnVisible();
                }
            });
        }
    }

    /**
     * Show or hide add post or comming button according to user location
     */
    private void updateBtnVisible(){
        Log.d(TAG, "PlaceActivity.updateBtnVisible()");

        // Check if the user can post in this place
        if (MyApplication.hasLastLocation() && CacheData.isAllowedToAddPost() && place.isReachable()){
            addPostButton.setVisibility(View.VISIBLE);
            comingButton.setVisibility(View.GONE);
        }
        else if (MyApplication.hasLastLocation() && CacheData.isAllowedToAddUserStatus(place.id, UserPlaceStatus.COMING)){
            comingButton.setVisibility(View.VISIBLE);
            addPostButton.setVisibility(View.GONE);
        }
        else{
            addPostButton.setVisibility(View.GONE);
            comingButton.setVisibility(View.GONE);
        }
    }

    //Menu Action Bar
    //////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_place, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_share:
                setDefaultShareIntent();
                return true;
            case R.id.action_reload:
                IntentsUtils.reload(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDefaultShareIntent() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_place_text));
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private void initPlaceAdapters() {
         //PlacesAdapter
        placesAdapter = new PlacesAdapter(this, false);
        placesAdapter.add(place);
        placeListView.setAdapter(placesAdapter);
    }

    public Place getPlace() {
        return place;
    }

    public int getPlaceId() {
        return placeId;
    }
}