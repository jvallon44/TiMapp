package com.timappweb.timapp.data.loader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.activeandroid.query.From;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.sync.performers.SyncAdapterOption;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.List;

/**
 * Created by stephane on 5/10/2016.
 */
public class MultipleEntryLoaderCallback<DataType> implements LoaderManager.LoaderCallbacks<List<DataType>> {

    private static final String TAG = "MultipleEntriesCallback";
    private final long syncDelay;
    private final From query;
    protected SyncAdapterOption syncOption;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private Context context;

    public MultipleEntryLoaderCallback(Context context,
                                       long syncDelay,
                                       int syncType,
                                       From query) {
        this.context = context;
        this.syncOption = new SyncAdapterOption(syncType);
        this.syncDelay = syncDelay;
        this.query = query;
    }

    @Override
    public Loader<List<DataType>> onCreateLoader(int id, Bundle args) {
        SyncBaseModel.getEntries(context, syncOption, query, syncDelay);
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(true);
        return new ModelLoader(context, User.class, query, false);
    }

    @Override
    public void onLoadFinished(Loader<List<DataType>> loader, List<DataType> data) {
        Log.d(TAG, "loaded finish");
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<List<DataType>> loader) {

    }

    public void onRefresh(){
        Log.v(TAG, "Refreshing data");
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(true);
        SyncBaseModel.getRemoteEntries(context, syncOption);
    }

    public void setSwipeAndRefreshLayout(SwipeRefreshLayout swipeAndRefreshLayout) {
        this.mSwipeRefreshLayout = swipeAndRefreshLayout;

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MultipleEntryLoaderCallback.this.onRefresh();
            }
        });
    }


}
