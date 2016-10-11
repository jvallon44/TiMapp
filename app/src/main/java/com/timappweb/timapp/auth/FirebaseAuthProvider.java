package com.timappweb.timapp.auth;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;

import retrofit2.Call;

/**
 * Created by Stephane on 24/09/2016.
 */
public class FirebaseAuthProvider {

    public static final String PROVIDER_ID = "firebase";
    public static final String PROVIDER_ID_FACEBOOK = "facebook";
    private static final String TAG = "FirebaseAuthProvider";

    private FirebaseAuth mAuth;
    private FirebaseLoginCallback callback;

    public FirebaseAuthProvider setCallback(FirebaseLoginCallback callback) {
        this.callback = callback;
        return this;
    }

    public void facebookLogin(LoginResult loginResult, Activity activity){
        String fbToken = loginResult.getAccessToken().getToken();
        AuthCredential credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
        JsonObject providerData = new JsonObject();
        providerData.addProperty("token", fbToken);
        this.firebaseLogin(credential,
                PROVIDER_ID_FACEBOOK,
                providerData,
                activity);
    }

    private Task<AuthResult> firebaseLogin(final AuthCredential credential,
                                           final String providerId,
                                           final JsonObject providerData,
                                           Activity activity) {
        if (mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            if (callback!= null) callback.onFirebaseLoginFailure(providerId, task.getException());
                            return;
                        }
                    }
                })
                .addOnSuccessListener(activity, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "signInWithCredential:onFirebaseLoginSuccess: " + authResult);
                        JsonObject payload = new JsonObject();
                        payload.add("firebase", createFirebasePayload(authResult));
                        JsonObject providerPayload = new JsonObject();
                        providerPayload.add("data", providerData);
                        providerPayload.addProperty("id", providerId);
                        payload.add("provider", providerPayload);
                        FirebaseAuthProvider.this.serverLogin(payload);
                    }
                });
    }


    private HttpCallManager serverLogin(final JsonObject payload){
        Log.i(TAG, "Request localLogin with payload=" + payload);

        return MyApplication.getAuthManager()
                .logWith(new AuthManager.LoginMethod<JsonObject>() {
                    @Override
                    public Call<JsonObject> login(JsonObject data) {
                        return RestClient.service().firebaseLogin(data);
                    }

                    @Override
                    public void cancelLogin() {

                    }
                }, payload);
    }

    public JsonObject createFirebasePayload(AuthResult authResult){
        JsonObject firebaseInfo = new JsonObject();
        firebaseInfo.addProperty("display_name", authResult.getUser().getDisplayName());
        firebaseInfo.addProperty("email", authResult.getUser().getEmail());
        firebaseInfo.addProperty("provider_id", authResult.getUser().getProviderId());
        firebaseInfo.addProperty("uuid", authResult.getUser().getUid());
        return firebaseInfo;
    }


    // ---------------------------------------------------------------------------------------------

    public interface FirebaseLoginCallback{

        void onFirebaseLoginSuccess(String providerId);

        void onFirebaseLoginFailure(String providerId, Throwable exception);

    }
}
