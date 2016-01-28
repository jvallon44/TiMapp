package com.timappweb.timapp.rest;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.data.LocalPersistenceManager;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.model.RestFeedback;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Created by stephane on 8/21/2015.
 * Handle a connection with the webservice.
 * Simulate a session with the server when the user is logged in.
 *
 * http://blog.robinchutaux.com/blog/a-smart-way-to-use-retrofit/
 */
public class RestClient {

    private static final String TAG = "RestClient";
    private static final String SQL_DATE_FORMAT = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'";

    //private static final String SQL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:SSSZ"; // http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
    private static RestClient conn = null;
    private final Application app;

    // All Shared Preferences Keys
    public static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_TOKEN = "token";

    // Current user
    private static final String CURRENT_USER = "current_user";
    private static ExecutorService mExecutorService = null;


    // KEY ID
    //public static final String KEY_SESSION_ID = "id";

    public static RestClient instance(){
        return conn;
    }

    public static WebServiceInterface service(){
        return conn.getService();
    }

    public static void init(Application app, String ep){
        conn = new RestClient(app, ep);
    }

    protected WebServiceInterface service;

    private static Retrofit.Builder builder = null;

    protected RestClient(Application app, String endpoint){
        this.app = app;

        Log.i(TAG, "Initializing server connection at " + endpoint);
        Gson gson = new GsonBuilder()
                .setDateFormat(SQL_DATE_FORMAT)
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(new SessionRequestInterceptor());
        // Executor use to cancel pending request to the server
        // http://stackoverflow.com/questions/18131382/using-squares-retrofit-client-is-it-possible-to-cancel-an-in-progress-request
        mExecutorService = Executors.newCachedThreadPool();
        builder = new Retrofit.Builder()
                //.setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .baseUrl(endpoint)
                .client(httpClientBuilder.build());
                //.setRequestInterceptor(new SessionRequestInterceptor())
                //.setConverter(new GsonConverter(gson))
                //.setExecutors(mExecutorService, new MainThreadExecutor());


        this.createService();
        Log.i(TAG, "Create connection with web service done!");

    }

    private void createService(){
        this.service =  builder.build().create(WebServiceInterface.class);
    }


    public WebServiceInterface getService(){
        return this.service;
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin() {
        // Check login status
        if(!MyApplication.isLoggedIn()){
            Log.d(TAG, "Checking login failed. Redirected to login page");
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(app, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Staring Login Activity
            app.startActivity(i);
        }

        Log.d(TAG, "Login success!");
    }
    /**
     * Create login session
     **/
    public void createLoginSession(String token, User user){
        user.writeToPref();
        LocalPersistenceManager.instance.editor.putString(KEY_TOKEN, token);
        LocalPersistenceManager.instance.editor.putBoolean(IS_LOGIN, true);
        LocalPersistenceManager.instance.editor.commit();

        // Update the service
        this.createService();
    }

    /**
     * Get stored session data
     * */
    public User getCurrentUser(){
        User user = new User();
        user.loadFromPref();
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        LocalPersistenceManager.instance.editor.clear();
        LocalPersistenceManager.instance.editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(app, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        app.startActivity(i);
    }

    /**
     * Check login on the server side thanks to the token
     */
    public void checkToken() {
        Log.i(TAG, "Checking user token...");
        final RestClient that = this;
        Call<RestFeedback> call = this.service.checkToken();
        call.enqueue(new Callback<RestFeedback>() {
            @Override
            public void onResponse(retrofit2.Response<RestFeedback> response) {
                if (!response.isSuccess() || !response.body().success) {
                    Log.i(TAG, "Token is not valid anymore. Login out");
                    MyApplication.logout();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i(TAG, "Token is not valid anymore. Login out");
                MyApplication.logout();
            }
        });
    }


    public static void stopPendingRequest() {
        List<Runnable> pendingAndOngoing = mExecutorService.shutdownNow();
        Log.d(TAG, "Stopping " + pendingAndOngoing.size() + " request(s) to the server");
    }
}

