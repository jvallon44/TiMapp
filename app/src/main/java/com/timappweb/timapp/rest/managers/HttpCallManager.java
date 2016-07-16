package com.timappweb.timapp.rest.managers;

import com.timappweb.timapp.rest.callbacks.HttpCallbackBase;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestErrorCallback;

import retrofit2.Call;

/**
 * Created by stephane on 6/6/2016.
 */
public class HttpCallManager {

    private final HttpCallbackBase<Object> callbackManager;
    private final Call call;

    public HttpCallManager(Call call) {
        this.call = call;
        this.callbackManager = new HttpCallbackBase<>();
    }

    public HttpCallManager onResponse(HttpCallback httpCallback) {
        if (this.callbackManager.getResponse() != null){
            this.callbackManager.onResponse(call);
        }
        else{
            this.callbackManager.add(httpCallback);
        }
        return this;
    }
    public HttpCallManager onError(RequestErrorCallback requestErrorCallback) {
        return this;
    }

    public HttpCallManager perform(){
        this.call.enqueue(this.callbackManager);
        return this;
    }

    public void cancel(){
        this.call.cancel();
    }
}
