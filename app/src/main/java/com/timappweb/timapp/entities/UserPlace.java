package com.timappweb.timapp.entities;

import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.adapters.EventUsersAdapter;
import com.timappweb.timapp.utils.Util;

import java.util.List;

public class UserPlace implements PlaceUserInterface{

    public int place_id;
    public UserPlaceStatus status;
    public int user_id;
    public int created;

    @SerializedName("User")
    public User user;

    @Override
    public String toString() {
        return "UserPlace{" +
                "place_id=" + place_id +
                ", status=" + status +
                ", user_id=" + user_id + " (" + user + ")" +
                ", created=" + created +
                '}';
    }

    @Override
    public List<Tag> getTags() {
        return null;
    }

    @Override
    public String getTimeCreated() {
        return Util.secondsTimestampToPrettyTime(this.created);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public int getViewType() {
        if(status==UserPlaceStatus.HERE) {
            return EventUsersAdapter.VIEW_TYPES.HERE;
        } else if(status==UserPlaceStatus.COMING){
            return EventUsersAdapter.VIEW_TYPES.COMING;
        } else if(status==UserPlaceStatus.INVITED){
            return EventUsersAdapter.VIEW_TYPES.INVITED;
        } else {
            return EventUsersAdapter.VIEW_TYPES.UNDEFINED;
        }
    }
}
