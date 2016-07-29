package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.adapters.EventsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.listeners.OnExploreTabSelectedListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.twotoasters.jazzylistview.effects.CardsEffect;
import com.twotoasters.jazzylistview.effects.FadeEffect;
import com.twotoasters.jazzylistview.effects.FlyEffect;
import com.twotoasters.jazzylistview.effects.SlideInEffect;
import com.twotoasters.jazzylistview.effects.TiltEffect;
import com.twotoasters.jazzylistview.effects.WaveEffect;
import com.twotoasters.jazzylistview.effects.ZipperEffect;
import com.twotoasters.jazzylistview.recyclerview.JazzyRecyclerViewScrollListener;

import java.util.List;

public class ExploreEventsFragment extends Fragment implements OnExploreTabSelectedListener {

    private static final String TAG = "ExplorePlaceFragment";
    private EventsAdapter eventsAdapter;
    private ExploreFragment exploreFragment;
    private DrawerActivity drawerActivity;
    private View newEventButton;
    private View progressView;
    private View noEventsView;
    private RecyclerView eventRecyclerView;
    private Button btnRequestNavigation;
    //private EachSecondTimerTask eachSecondTimerTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_explore_events, container, false);

        exploreFragment = (ExploreFragment) getParentFragment();
        drawerActivity = (DrawerActivity) exploreFragment.getActivity();

        //Views
        eventRecyclerView = (RecyclerView) root.findViewById(R.id.list_places);
        progressView = root.findViewById(R.id.loading_view);
        noEventsView = root.findViewById(R.id.no_events_view);
        /*newEventButton = root.findViewById(R.id.post_event_button);
        newEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.locate(getContext());
            }
        });*/

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsAdapter = new EventsAdapter(getContext());
        eventRecyclerView.setAdapter(eventsAdapter);
        eventsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.viewSpecifiedEvent(getContext(), eventsAdapter.getItem(position));
            }
        });

        //set Jazzy Effect on Recycler View
        //Best effects : CardsEffect(), TiltEffect(), ZipperEffect().
        JazzyRecyclerViewScrollListener jazzyRecyclerViewScrollListener = new JazzyRecyclerViewScrollListener();
        jazzyRecyclerViewScrollListener.setTransitionEffect(new TiltEffect());
        eventRecyclerView.addOnScrollListener(jazzyRecyclerViewScrollListener);
    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");

        /*eachSecondTimerTask = EachSecondTimerTask.add(new TimeTaskCallback() {
            @Override
            public void update() {
                eventsAdapter.notifyDataSetChanged();
            }
        })
*/
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        //eachSecondTimerTask.cancel();
    }

    @Override
    public void onTabSelected() {
        if (eventsAdapter == null) return;
        Log.d(TAG, "ExploreEventsFragment is now selected");
        Log.d(TAG, "Loading "+ eventsAdapter.getData().size()+" places in List");
        eventsAdapter.clear();
        ExploreMapFragment exploreMapFragment = exploreFragment.getExploreMapFragment();
        List<Event> markers = exploreFragment.getAreaRequestHistory().getInsideBoundsItems(exploreMapFragment.getMapBounds());
        eventsAdapter.setData(markers);

        if(eventsAdapter.getData().size()==0) {
            noEventsView.setVisibility(View.VISIBLE);
        } else {
            noEventsView.setVisibility(View.GONE);
        }
    }

}
