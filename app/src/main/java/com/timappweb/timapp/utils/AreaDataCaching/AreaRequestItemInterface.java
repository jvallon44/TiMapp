package com.timappweb.timapp.utils.AreaDataCaching;

import java.util.List;

import retrofit2.Call;

/**
 * Created by stephane on 9/13/2015.
 * Represents a request for a data area
 *
 *
 *
 */
public interface AreaRequestItemInterface<T> {

    void setDataTimestamp(long timesamp);

    void updateLocalTimestamp();

    void update(AreaRequestItemInterface<T> item);

    int getLastUpdateDelay();

    int setPendingCall(Call<List<T>> pendingCall);

    boolean isOutdated(int itemRequestId);

    void cancel();

    void setData(List<T> data);

    void clear();

    long getDataTimestamp();

    List<T> getData();

    int getRequestId();
}