package com.timappweb.timapp.sync.performers;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.sync.DataSyncAdapter;

/**
 * Created by stephane on 5/12/2016.
 */
public class SyncAdapterOption {

    private Bundle bundle;

    public SyncAdapterOption(int type) {
        bundle = new Bundle();
        setType(type);
    }

    public void setType(int type){
        bundle.putInt(DataSyncAdapter.SYNC_TYPE_KEY, type);
    }

    public int getSyncType() {
        return bundle.getInt(DataSyncAdapter.SYNC_TYPE_KEY);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setLastSyncTime() {
        bundle.putLong(DataSyncAdapter.LAST_SYNC_TIME, SyncHistory.getLastSyncTime(getSyncType()));
    }

    public void set(String key, LatLngBounds bounds) {
        bundle.putDouble(key + "swlatitude", bounds.southwest.latitude);
        bundle.putDouble(key + "swlongitude", bounds.southwest.longitude);
        bundle.putDouble(key + "nelatitude", bounds.northeast.latitude);
        bundle.putDouble(key + "nelongitude", bounds.northeast.longitude);
    }
}
