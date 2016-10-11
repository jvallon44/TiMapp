package com.timappweb.timapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.managers.MultipleHttpCallManager;
import com.timappweb.timapp.utils.deeplinks.DeepLinkParser;
import com.timappweb.timapp.utils.deeplinks.UrlParser;
import com.timappweb.timapp.views.RetryDialog;

/**
 * Created by stephane on 3/26/2016.
 */
public class SplashActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SplashActivity";
    private static final String CALL_ID_TOKEN = "check_token";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Build GoogleApiClient with AppInvite API for receiving deep links
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        this.loadConfig();
    }

    private void loadConfig() {

        final MultipleHttpCallManager callsManager = ConfigurationProvider.load(this);


        if (MyApplication.isLoggedIn() && ConfigurationProvider.hasRulesConfig()) {
            HttpCallManager tokenCallManager = MyApplication.auth.checkToken();
            if (tokenCallManager != null) {
                callsManager.addCall(CALL_ID_TOKEN, tokenCallManager);
            }
        }

        callsManager
                .setCallback(new MultipleHttpCallManager.Callback() {
                    @Override
                    public void onPostExecute() {
                        // TODO do not retry everything
                        if (!ConfigurationProvider.hasFullConfiguration()){
                            Log.e(TAG, "Cannot load server configuration");
                            if (!ConfigurationProvider.hasRulesConfig()) {
                                Log.e(TAG, "    - No rules configuration");
                            }
                            if (!ConfigurationProvider.hasSpotCategoriesConfig()) {
                                Log.e(TAG, "    - No spot categories configuration");
                            }
                            if (!ConfigurationProvider.hasEventCategoriesConfig()) {
                                Log.e(TAG, "    - No event categories configuration");
                            }
                            ConfigurationProvider.clearStaticVariables();
                            RetryDialog.builder(SplashActivity.this, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            SplashActivity.this.loadConfig();
                                        }
                                    }, getString(R.string.cannot_load_server_configuration_title),
                                    getString(R.string.cannot_load_server_configuration_msg))
                                    .setCancelable(false)
                                    .create()
                                    .show();
                        }
                        else{
                            if (callsManager.isSuccess(ConfigurationProvider.CALL_ID_SPOT_CATEGORIES)
                                    && callsManager.isSuccess(ConfigurationProvider.CALL_ID_APPLICATION_RULES)
                                    && callsManager.isSuccess(ConfigurationProvider.CALL_ID_EVENT_CATEGORIES)) {
                                ConfigurationProvider.updateLastUpdateTime();
                            }

                            SplashActivity.this.onConfigLoaded();

                        }
                    }

                    @Override
                    public void onAllSuccess() {
                    }
                })
                .perform();

    }

    private void onConfigLoaded() {
        this.checkDeepLink();
    }

    private void continueToActivity(){
        if (MyApplication.isLoggedIn()){
            Event currentEvent = EventStatusManager.getCurrentEvent();
            if (currentEvent != null && currentEvent.isAccessible()){
                IntentsUtils.viewSpecifiedEvent(SplashActivity.this, currentEvent);
            }
            else{
                IntentsUtils.home(SplashActivity.this);
            }
        }
        else if (MyApplication.isFirstLaunch()){
            Log.i(TAG, "User starting app for the first time");
            IntentsUtils.presentApp(SplashActivity.this);
        }
        else{
            IntentsUtils.login(SplashActivity.this, true);
        }
        MyApplication.updateLastLaunch();
        finish();
    }

    private void checkDeepLink(){
        boolean autoLaunchDeepLink = true;
        new DeepLinkParser()
                .addMatcher(new DeepLinkParser.DeepLinkCallback() {
                    @Override
                    public String getMask() {
                        return "/events/view/:eventId";
                    }

                    @Override
                    public boolean onMatch(UrlParser urlParser) {
                        Long eventId = Long.valueOf(urlParser.getPart("eventId"));
                        Log.d(TAG, "Deep link viewing event: " + eventId);
                        if (eventId != null && eventId > 0) {
                            IntentsUtils.viewEventFromId(SplashActivity.this, eventId);
                            return true;
                        }
                        return false;
                    }
                })
                .setNoMatchCallback(new DeepLinkParser.NoMatchCallback() {
                    @Override
                    public void onNoMatch() {
                        SplashActivity.this.continueToActivity();
                    }
                })
                .parse(mGoogleApiClient, this, autoLaunchDeepLink);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "ERROR CONNECTING: " + connectionResult);
        // TODO do we need to proceed to activity ?
        this.continueToActivity();
    }
}
