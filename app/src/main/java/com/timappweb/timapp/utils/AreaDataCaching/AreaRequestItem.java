package com.timappweb.timapp.utils.AreaDataCaching;

import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.utils.Util;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 9/13/2015.
 * Represents a request for a data area
 *
 */
public class AreaRequestItem<T> {
    public int dataTimestamp;       // Timestamp on the server
    public int localTimestamp;      // Timestamp on the local machine

    public List<T> data;         // LIFO: Last spot in => First spot out
    //public boolean isDisplayed = false;    // True if it's display on the map

    public AreaRequestItem(int dataTimestamp, List<T> spots) {
        this.setDataTimestamp(dataTimestamp);
        this.updateLocalTimestamp();
        this.data = spots;
    }

    public AreaRequestItem() {
        this.data = new LinkedList<>();
        this.dataTimestamp = 0;
        this.localTimestamp = 0;
    }


    public void setDataTimestamp(int timesamp) {
        this.dataTimestamp = timesamp;
    }
    public void updateLocalTimestamp() {
        this.localTimestamp = Util.getCurrentTimeSec(); // OK with only 32 bits
    }
    @Override
    public String toString() {
        return "AreaRequestItem{" +
                "dataTimestamp=" + dataTimestamp +
                ", nb spots=" + this.data.size() +
                '}';
    }

    public void update(AreaRequestItem item) {
        this.setDataTimestamp(dataTimestamp);
        data.addAll(item.data);
    }

    /**
     *
     * @return
     */
    public int getLastUpdateDelay() {
        return Util.getCurrentTimeSec() - this.localTimestamp;
    }
}