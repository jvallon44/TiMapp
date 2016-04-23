package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.EventUsersHeaderAdapter;
import com.timappweb.timapp.adapters.SimpleSectionedRecyclerViewAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.PlaceUserInterface;
import com.timappweb.timapp.entities.PlacesInvitation;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.UserPlace;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.ApiCallFactory;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.PaginationResponse;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.jdeferred.impl.DeferredObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;


public class PlacePeopleFragment extends PlaceBaseFragment {

    private static final String TAG = "PlaceTagsFragment";
    private Context         context;
    private PlaceActivity placeActivity;
    private Place place;
    private int placeId;

    private EventUsersHeaderAdapter placeUsersAdapter;
    private RecyclerView    peopleRv;
    private View            progressView;
    private View            noPostsView;
    private View            noConnectionView;
    private View            mainButton;
    private TextView        tvAddButton;

    //private List<Post> posts;
    //private List<UserPlace> peopleComing;
    //private List<UserPlace> peopleInvited;

   // private ArrayList<PlaceUserInterface> usersFullList;
    private DeferredObject deferred;
    private int loadCount = 0;
    private SimpleSectionedRecyclerViewAdapter mSectionedAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_place_people, container, false);

        initVariables(root);
        initAdapter();
        initRv();
        setListeners();
        updateBtnVisibility();

        return root;
    }



    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }

    private void initVariables(View root) {
        placeActivity = (PlaceActivity) getActivity();
        context= placeActivity.getBaseContext();

        //Views
        mainButton = root.findViewById(R.id.main_button);
        tvAddButton = (TextView) root.findViewById(R.id.text_main_button);
        peopleRv = (RecyclerView) root.findViewById(R.id.list_people);
        progressView = root.findViewById(R.id.progress_view);
        noPostsView = root.findViewById(R.id.no_posts_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
    }

    private void initRv() {
        peopleRv.setLayoutManager(new LinearLayoutManager(context));

        // Add the sticky headers decoration
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(placeUsersAdapter);
        peopleRv.addItemDecoration(headersDecor);

        //TODO : Determine how the class DividerDecoration is usefull, and decide if we use it or not
        //peopleRv.addItemDecoration(new DividerDecoration(placeActivity));
    }

    private void setListeners() {
        mainButton.setOnClickListener(placeActivity.getPeopleListener());
    }


    private void initAdapter() {
        //Construct Adapter
        placeUsersAdapter = new EventUsersHeaderAdapter(context);
        placeUsersAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.v(TAG, "Accessing position: " + position);
                PlaceUserInterface user = placeUsersAdapter.getData(position);
                Log.d(TAG, "Viewing profile user: " + user.getUser());
                IntentsUtils.profile(placeActivity, user.getUser());
            }
        });
        //placeUsersAdapter.create("post", getResources().getString(R.string.header_posts));
        //placeUsersAdapter.create(UserPlaceStatus.COMING, getResources().getString(R.string.header_coming));
        //placeUsersAdapter.create(UserPlaceStatus.INVITED, getResources().getString(R.string.header_invited));

        /*mSectionedAdapter = new SimpleSectionedRecyclerViewAdapter(
                context,
                R.layout.header_place_people,
                R.id.text_header_place_people,
                placeUsersAdapter);*/

        peopleRv.setAdapter(placeUsersAdapter);
    }

    public void loadData() {
        placeUsersAdapter.clear();
        loadPosts();
        loadByStatus(UserPlaceStatus.COMING);

        if (MyApplication.isLoggedIn()){
            loadInvites();
        }
    }


    private void loadPosts() {
        Call<List<Post>> call = RestClient.service().viewPostsForPlace(placeActivity.getPlaceId());
        RestCallback callback = new RestCallback<List<Post>>(getContext()) {
            @Override
            public void onResponse200(Response<List<Post>> response) {
                placeUsersAdapter.addData(response.body());
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                noConnectionView.setVisibility(View.VISIBLE);
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }
        };
        asynCalls.add(ApiCallFactory.build(call, callback, this));
    }


    private void loadByStatus(final UserPlaceStatus status){
        Map<String, String> conditions = new HashMap<>();
        conditions.put("status", String.valueOf(status));

        Call<PaginationResponse<UserPlace>> call = RestClient.service().viewUsersForPlace(placeActivity.getPlaceId(), conditions);
        call.enqueue(new RestCallback<PaginationResponse<UserPlace>>(getContext(), this) {
            @Override
            public void onResponse200(Response<PaginationResponse<UserPlace>> response) {
                placeUsersAdapter.addData(response.body().items);
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                noConnectionView.setVisibility(View.VISIBLE);
            }

        });
    }

    private void loadInvites(){
        Call<PaginationResponse<PlacesInvitation>> call = RestClient.service().invitesSent(placeActivity.getPlaceId());
        call.enqueue(new RestCallback<PaginationResponse<PlacesInvitation>>(getContext()) {

            @Override
            public void onResponse200(Response<PaginationResponse<PlacesInvitation>> response) {
                List<PlacesInvitation> invitations = response.body().items;
                Log.d(TAG, "Loading " + invitations.size() + " invites sent");
                placeUsersAdapter.addData(invitations);
            }

        });
        asynCalls.add(call);
    }


    public void updateBtnVisibility() {
        mainButton.setVisibility(MyApplication.isLoggedIn() && placeActivity.isUserAround() ? View.VISIBLE : View.GONE);
    }

    public void setProgressView(boolean visibility) {
        if(visibility) {
            progressView.setVisibility(View.VISIBLE);
            peopleRv.setVisibility(View.GONE);
            noConnectionView.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            peopleRv.setVisibility(View.VISIBLE);
            noConnectionView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
        placeUsersAdapter.clear();
        loadData();
    }
}
