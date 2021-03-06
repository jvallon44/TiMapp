package com.timappweb.timapp.rest.io.responses;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 9/12/2015.
 */
public class RestFeedback extends RestResponse {

    @Expose
    @SerializedName("message")
    public String message;

    @Expose
    @SerializedName("data")
    public JsonObject data;

    @Expose
    @SerializedName("validationErrors")
    public JsonObject errors;

    // =============================================================================================

    public String toString(){
        return "ServerObject[message=" + message + ";]";
    }

}
