package com.timappweb.timapp.rest;

import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;

/**
 * Created by Jack on 28/01/2016.
 */
public class PostAndPlaceRequest {

    @SerializedName("post")
    private Post post;

    @SerializedName("place")
    private Place place;

    public PostAndPlaceRequest(Post post, Place place) {
        this.post = post;
        this.place = place;
    }
}
