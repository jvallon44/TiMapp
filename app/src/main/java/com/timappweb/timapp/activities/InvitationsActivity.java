package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.InvitationsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.PlacesInvitation;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.List;

public class InvitationsActivity extends BaseActivity{

    private String TAG = "ListFriendsActivity";
    private List<PlacesInvitation> invitations;
    private RecyclerView recyclerView;
    private InvitationsAdapter adapter;
    private View noInvitationsView;

    private View progressView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating ListFriendsActivity");
        setContentView(R.layout.activity_invitations);
        this.initToolbar(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        noInvitationsView = findViewById(R.id.no_invitations_view);
        progressView = findViewById(R.id.loading_invitations);

        initAdapterListFriends();
        loadInvitations();
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        adapter = new InvitationsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                onItemListClicked(position);
            }
        });
    }

    private void loadInvitations(){
        //TODO StephyLeFoufy : Load Invitations

        //Dummy
        List<PlacesInvitation> invitationsLoaded = new ArrayList<>();
        PlacesInvitation dummyPlace = new PlacesInvitation(Place.createDummy(), User.createDummy());
        invitationsLoaded.add(dummyPlace);
        PlacesInvitation dummyPlace2 = new PlacesInvitation(Place.createDummy(), User.createDummy());
        invitationsLoaded.add(dummyPlace2);

        onInvitationsLoaded(invitationsLoaded);
        progressView.setVisibility(View.GONE);
    }

    private void onInvitationsLoaded(List<PlacesInvitation> items){

        invitations = items;
        if(invitations.size()==0) {
            noInvitationsView.setVisibility(View.VISIBLE);
        } else {
            noInvitationsView.setVisibility(View.GONE);
            adapter.setData(items);
        }
        progressView.setVisibility(View.GONE);
    }

    private void onItemListClicked(int position) {
        PlacesInvitation invitation = invitations.get(position);
        IntentsUtils.viewSpecifiedPlace(this, invitation.place);
    }
}