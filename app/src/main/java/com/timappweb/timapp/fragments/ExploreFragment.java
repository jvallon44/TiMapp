package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderFromAPI;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;

public class ExploreFragment extends Fragment{

    private static final String TAG = "ExploreFragment";

    private AreaDataLoaderFromAPI dataLoader;


    private FrameLayout containerEvents;
    private View blurBackground;
    private ExploreEventsFragment eventsFragment;
    public ExploreMapFragment mapFragment;
    private View fab;

    private Menu menu;
    private View.OnClickListener fabClickListener;

    public ExploreMapFragment getExploreMapFragment(){
        return mapFragment;
    }

    public AreaDataLoaderFromAPI getDataLoader() {
        return dataLoader;
    }

    public AreaRequestHistory getAreaRequestHistory() {
        return mapFragment.getHistory();
    }

    /*
    public void reloadMapData(){
        if(getExploreMapFragment()!=null) {
            dataLoader.clear();
            getExploreMapFragment().updateMapData();
        }
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore, container, false);
        setHasOptionsMenu(true);

        containerEvents = (FrameLayout) root.findViewById(R.id.fragment_events);
        blurBackground = root.findViewById(R.id.blur_background);

        dataLoader = new AreaDataLoaderFromAPI(this.getActivity(), MyApplication.searchFilter);

        initAddSpotButton();
        initFragments();
        setListeners();

        return root;
    }

    public void initAddSpotButton() {
        fabClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.locate(getActivity());
            }
        };
    }

    private void initFragments() {
        //Map
        FragmentTransaction transactionMap = getChildFragmentManager().beginTransaction();
        mapFragment = new ExploreMapFragment();
        transactionMap.add(R.id.fragment_map, mapFragment);
        transactionMap.commit();

        //List
        FragmentTransaction transactionList = getChildFragmentManager().beginTransaction();
        eventsFragment = new ExploreEventsFragment();
        transactionList.add(R.id.fragment_events, eventsFragment);
        transactionList.commit();
    }

    private void setListeners() {
        dataLoader.setLoadingListener(new LoadingListener() {
            @Override
            public void onLoadStart() {
                if (mapFragment != null) {
                    mapFragment.setLoaderVisibility(true);
                    if(mapFragment.getSelectingMarker() != null) {
                        mapFragment.getSelectingMarker().setVisible(false);
                    }
                }
            }

            @Override
            public void onLoadEnd() {
                if (mapFragment != null) {
                    mapFragment.setLoaderVisibility(false);
                    if(mapFragment.getSelectingMarker() != null) {
                        mapFragment.getSelectingMarker().setVisible(true);
                    }
                }
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                actionList();
                return true;
            case R.id.action_open_filter:
                IntentsUtils.filter(getActivity());
                return true;
            case R.id.action_clear_filter:
                MyApplication.searchFilter.tags.clear();
                mapFragment.updateFilterView();
                mapFragment.updateMapData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    

    public void actionList() {
        if(containerEvents.getVisibility()==View.GONE) {
            boolean isEventsOnMap = eventsFragment.onFragmentSelected(this);
            if(isEventsOnMap) {
                Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_down);
                Animation appear = AnimationUtils.loadAnimation(getContext(), R.anim.appear);
                blurBackground.startAnimation(appear);
                containerEvents.startAnimation(slideIn);
                containerEvents.setVisibility(View.VISIBLE);
                blurBackground.setVisibility(View.VISIBLE);
                menu.findItem(R.id.action_list).setIcon(ContextCompat.getDrawable(getContext(), R.drawable.list));
            } else {
                Toast.makeText(getContext(), R.string.text_no_event, Toast.LENGTH_SHORT).show();
            }
        } else {
            Animation slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
            Animation disappear = AnimationUtils.loadAnimation(getContext(), R.anim.disappear);
            blurBackground.startAnimation(disappear);
            containerEvents.startAnimation(slideOut);
            containerEvents.setVisibility(View.GONE);
            blurBackground.setVisibility(View.GONE);
            menu.findItem(R.id.action_list).setIcon(ContextCompat.getDrawable(getContext(), R.drawable.list));
        }
    }

    public View.OnClickListener getFabClickListener() {
        return fabClickListener;
    }


    public ExploreEventsFragment getExploreEventsFragment() {
        return eventsFragment;
    }
    public FrameLayout getContainerEvents() {
        return containerEvents;
    }
}

