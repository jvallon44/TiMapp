package com.timappweb.timapp.utils;

import com.timappweb.timapp.entities.Post;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 9/13/2015.
 * Represents a request for a data area
 *
 */
public class AreaRequestItem {
    public int timestamp;       // Request timestamp
    public int localTimestamp;
    public List<Post> data;    // LIFO: Last spot in => First spot out
    //public boolean isDisplayed = false;    // True if it's display on the map

    public AreaRequestItem(int timestamp, List<Post> spots) {
        this.setTimesamp(timestamp);
        this.data = spots;
    }

    public AreaRequestItem() {
        this.data = new LinkedList<>();
    }


    public void setTimesamp(int timesamp) {
        this.timestamp = timesamp;
        this.localTimestamp = (int)(System.currentTimeMillis() / 1000); // OK with only 32 bits
    }
    @Override
    public String toString() {
        return "AreaRequestItem{" +
                "timestamp=" + timestamp +
                ", nb spots=" + this.data.size() +
                '}';
    }

    public void update(AreaRequestItem item) {
        this.setTimesamp(timestamp);
        data.addAll(item.data);
    }

    public int getLastUpdateDelay() {
        return (int)(System.currentTimeMillis() / 1000) - this.localTimestamp;
    }
}