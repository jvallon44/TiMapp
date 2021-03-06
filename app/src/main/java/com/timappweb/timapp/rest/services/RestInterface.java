package com.timappweb.timapp.rest.services;

import com.google.gson.JsonObject;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;

import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by stephane on 8/20/2015.
 */
public interface RestInterface {

    @POST("/api/{model}/add.json")
    Call<JsonObject> post(@Path("model") String model, @Body JsonObject data);

    @Multipart
    @POST("/api/{model}/add.json")
    Call<JsonObject> post(@Path("model") String model, @Part("data") JsonObject data, @Part("picture") RequestBody file);

    @PUT("/api/{model}/edit/{id}.json")
    Call<JsonObject> put(@Path("model") String model, @Path("id") long id, @Body JsonObject data);

    @GET("/api/{model}.json")
    <T> Call<PaginatedResponse<T>> paginate(@Path("model") String model);

    @GET("/api/{model}.json")
    <T> Call<PaginatedResponse<T>> paginate(@Path("model") String model, @QueryMap HashMap<String, String> options);

    @GET("/api/{model}.json")
    <T> Call<List<T>> list(@Path("model") String model);

    @GET("/api/{model}.json")
    <T> Call<List<T>> list(@Path("model") String model, @QueryMap HashMap<String, String> options);

    @GET("/api/{model}/view/{id}.json")
    <T> Call<List<T>> get(@Path("model") String model, @Path("id") long id);

    @GET("/api/{model}/view/{id}.json")
    <T> Call<List<T>> get(@Path("model") String model, @Path("id") long id, @QueryMap HashMap<String, String> options);

}
