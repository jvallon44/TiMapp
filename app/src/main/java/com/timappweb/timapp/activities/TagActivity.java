package com.timappweb.timapp.activities;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.DataTransformTag;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.Post;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.OnBasicQueryTagListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.listeners.OnThreeQueriesTagListener;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.managers.SearchTagDataProvider;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.EventView;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;

public class TagActivity extends BaseActivity{

    private String TAG = "TagActivity";

    //Views
    private HorizontalTagsRecyclerView selectedTagsRV;
    private View progressBarView;
    private EventView eventView;
    private Place currentPlace = null;
    private View progressEndView;

    // @Bind(R.remote_id.hashtags1)
    protected HashtagView suggestedTagsView;

    //others
    private SearchAndSelectTagManager searchAndSelectTagManager;
    private View selectedTagsView;
    private Post currentPost;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check that we gave the place as an extra parameter
        this.currentPlace = IntentsUtils.extractPlace(getIntent());
        this.currentPost = IntentsUtils.extractPost(getIntent());
        if (this.currentPlace == null || this.currentPost == null){
            Log.d(TAG, "Place is null");
            IntentsUtils.home(this);
            return;
        }

        setContentView(R.layout.activity_tag);
        this.initToolbar(false);

        //Initialize variables
        selectedTagsView = findViewById(R.id.rv_selected_tags);
        selectedTagsRV = (HorizontalTagsRecyclerView) selectedTagsView;
        suggestedTagsView = (HashtagView) findViewById(R.id.rv_search_suggested_tags);
        progressBarView = findViewById(R.id.progress_view);
        progressEndView = findViewById(R.id.progress_end);
        confirmButton = (Button) findViewById(R.id.confirm_button);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setProgressView(true);
                currentPost.place_id = currentPlace.remote_id;
                // Validating user input
                if (!currentPost.validateForSubmit()) {
                    Toast.makeText(TagActivity.this, "Invalid inputs", Toast.LENGTH_LONG).show(); // TODO proper message
                    return;
                }
                Log.d(TAG, "Submitting post: " + currentPost);

                Call<RestFeedback> call = RestClient.service().addPost(currentPost);
                call.enqueue(new AddPostCallback(TagActivity.this, currentPost, currentPlace));
            }
        });

        initClickSelectedTag();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tags, menu);

        setSearchview(menu);
        searchView.requestFocus();
        searchView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        initListTags();

        //set hint for searchview
        final OnBasicQueryTagListener onBasicQueryTagListener = new OnThreeQueriesTagListener(this);
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView,
                suggestedTagsView,
                selectedTagsRV,
                onBasicQueryTagListener,
                new SearchTagDataProvider() {
                    @Override
                    public void onLoadEnds() {
                        getProgressBarView().setVisibility(View.GONE);
                    }
                }
        );


        //Initialize Query hint in searchview
        actionCounter();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_validate:
                String query = searchView.getQuery().toString();
                if(query.isEmpty()) {
                    Toast.makeText(this, "ActionTypeName a tag before submitting", Toast.LENGTH_SHORT).show();
                }
                else {
                    searchView.setQuery(query, true);
                }
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IntentsUtils.REQUEST_PUBLISH && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }


    //----------------------------------------------------------------------------------------------
    //Private methods

    private void initListTags() {
        suggestedTagsView.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                Tag tag = (Tag) item;

                boolean success = suggestedTagsView.removeItem(item);
                Log.d(TAG, "removing item success : "+ success);

                searchView.setQuery(tag.name, true);
                searchView.clearFocus();
            }
        });

        suggestedTagsView.setData(new LinkedList<Tag>(), new DataTransformTag());
    }

    private void initClickSelectedTag() {
        selectedTagsRV.getAdapter().setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Clicked on selected item");
                Tag tag = selectedTagsRV.getAdapter().getData(position);
                suggestedTagsView.addItem(tag);
                selectedTagsRV.getAdapter().removeData(position);
                actionCounter();
            }
        });
    }

    public void actionCounter() {
        switch (searchAndSelectTagManager.getSelectedTags().size()) {
            case 0:
                setSelectedTagsViewGone();
                searchView.setQueryHint(getResources().getString(R.string.searchview_hint_3));
                break;
            case 1:
                setSelectedTagsViewVisible();
                searchView.setQueryHint(getResources().getString(R.string.searchview_hint_2));
                searchView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                break;
            case 2:
                searchView.setQueryHint(getResources().getString(R.string.searchview_hint_1));
                searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            case 3:
                searchView.clearFocus();
                suggestedTagsView.setVisibility(View.GONE);
                progressEndView.setVisibility(View.VISIBLE);
                currentPost.setTags(searchAndSelectTagManager.getSelectedTags());
                confirmButton.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    //Public methods
    private void setSelectedTagsViewGone() {
        findViewById(R.id.top_line_hrv).setVisibility(View.GONE);
        selectedTagsView.setVisibility(View.GONE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.GONE);
    }

    private void setSelectedTagsViewVisible() {
        findViewById(R.id.top_line_hrv).setVisibility(View.VISIBLE);
        selectedTagsView.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.VISIBLE);
    }

    public void simulateKeys() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
            }
        }).start();
    }


    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public View getProgressBarView() {
        return progressBarView;
    }



    //----------------------------------------------------------------------------------------------
    //Inner classes
    private class AddPostCallback extends RestFeedbackCallback {

        private Post post;
        private Place place;
        // TODO get post from server instead. (in case tags are in a black list)

        AddPostCallback(Context context, Post post, Place place) {
            super(context);
            this.post = post;
            this.place = place;
        }

        @Override
        public void onActionSuccess(RestFeedback feedback) {
            int id = Integer.valueOf(feedback.data.get("id"));
            int placeId = Integer.valueOf(feedback.data.get("place_id"));
            Log.i(TAG, "Post for place " + placeId + " has been saved with id: " + id);

            // Caching data
            post.place_id = placeId;
            post.created = Util.getCurrentTimeSec();
            if (place.isNew()){
                place.remote_id = placeId;
                place.created = Util.getCurrentTimeSec();
                QuotaManager.instance().add(QuotaType.PLACES);
            }
            //QuotaManager.instance().add(QuotaType.ActionTypeName.CREATE_PLACE);
            //CacheData.setLastPost(post);
            QuotaManager.instance().add(QuotaType.ADD_POST);

            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void onActionFail(RestFeedback feedback) {
            Toast.makeText(this.context, feedback.message, Toast.LENGTH_LONG).show();
            //setProgressView(false);
        }


        @Override
        public void onFailure(Throwable t) {
            super.onFailure(t);
            //setProgressView(false);
        }
    }

}
