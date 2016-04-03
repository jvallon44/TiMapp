package com.timappweb.timapp.activities;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.SocialProvider;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.services.InstanceIDService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;


/**
 * NewActivity login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "LoginActivity";

    /**
     * NewActivity dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "stef@tiemapp.com:stef", "jack@tiemapp.com:jack"
    };
    private static final int MINMUM_PASSWORD_LENGTH = 3;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
//    private EditText mPasswordView;
//    private View mProgressView;
//    private View mLoginFormView;

    private SimpleFacebook mSimpleFacebook;
    private View layoutFb;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initToolbar(false);

        layoutFb = findViewById(R.id.layout_fb);
        progressView = findViewById(R.id.progress_view);

        /*// Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Prefix for testing purpose
        if (BuildConfig.DEBUG){
            mEmailView.setText("t@m.com");
            mPasswordView.setText("test");
        }

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mLoginFormView = findViewById(R.id.sign_up_form);
        mProgressView = findViewById(R.id.login_progress);
*/
        setListeners();

        final Activity that = this;
        Button skipLogin = (Button) findViewById(R.id.skip_loggin_button);
        skipLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.home(that);
            }
        });
    }

    private void setListeners() {

        final Activity that = this;
        ImageButton loginButton = (ImageButton) findViewById(R.id.login_button);
        final OnLoginListener onLoginListener = new OnLoginListener() {

            @Override
            public void onLogin(final String accessToken, List<Permission> acceptedPermissions, List<Permission> declinedPermissions) {
                Log.i(TAG, "Logging in");
                setProgressVisibility(true);
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("access_token", accessToken);
                params.put("app_id", InstanceIDService.getId(that));

                Call<RestFeedback> call = RestClient.service().facebookLogin(params);
                call.enqueue(new RestFeedbackCallback(that) {
                    @Override
                    public void onActionSuccess(RestFeedback feedback) {
                        User user = new User("", "");
                        String token = feedback.data.get("token");
                        user.username = feedback.data.get("username");
                        user.provider_uid = feedback.data.get("social_id");
                        user.provider = SocialProvider.FACEBOOK;
                        user.id = Integer.parseInt(feedback.data.get("id"));
                        user.app_id = InstanceIDService.getId(that);
                        Log.i(TAG, "Session created with session token: " + token);
                        MyApplication.login(user, token, accessToken);
                        IntentsUtils.lastActivityBeforeLogin(that);
                    }

                    @Override
                    public void onActionFail(RestFeedback feedback) {
                        setProgressVisibility(false);
                        Log.i(TAG, "User attempt to connect with wrong credential: server message: " + feedback.message);
                    }
                });
            }

            @Override
            public void onCancel() {
                setProgressVisibility(false);
                Log.d(TAG, "Facebook connection canceled");
            }

            @Override
            public void onFail(String reason) {
                setProgressVisibility(false);
                Log.d(TAG, "Facebook connection failed : " + reason);
            }

            @Override
            public void onException(Throwable throwable) {
                setProgressVisibility(false);
                Log.d(TAG, "Facebook connection.. OnException: " + throwable.getMessage());
            }

        };

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutFb.setVisibility(View.GONE);
                mSimpleFacebook.login(onLoginListener);
            }
        });
    }


    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    /*public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            String loginStr = email+":"+password;
            if (BuildConfig.DEBUG &&
                    Arrays.asList(DUMMY_CREDENTIALS).contains(loginStr)){
                // Check dummy credential
                Log.i(TAG, "Login with dummy credential");
                RestClient.instance().createLoginSession("NewActivity", new User(email, ""));
                IntentsUtils.home(this);
            }
            else{
                //showProgress(true);
                mAuthTask = new UserLoginTask(this);
                mAuthTask.execute((Void) null);
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true; //email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > MINMUM_PASSWORD_LENGTH;
    }

    *//**
     * Shows the progress UI and hides the login form.
     *//*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     *
     * TODO: remove useless
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final Activity activity;

        /*UserLoginTask(Activity act, String email, String password) {
            mEmail = email;
            mPassword = password;
            this.activity = act;
        }*/

        UserLoginTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            User user = new User("test@mail.com", "test");
            Log.d(TAG, "Try login for user: " + user.toString());

            try{
                RestFeedback response = null; //= RestClient.service().login(user);
                Log.i(TAG, "Server response: " + response);

                if (response.success
                        && response.data.containsKey("token")
                        && response.data.containsKey("id")
                        && response.data.containsKey("username")){
                    String token = (String) response.data.get("token");
                    user.username = response.data.get("username");
                    user.id = Integer.parseInt(response.data.get("id"));
                    MyApplication.login(user, token, null);
                    Log.i(TAG, "Session created with session token: " + token);
                    IntentsUtils.lastActivityBeforeLogin(this.activity);
                    return true;
                }
                else if (!response.data.containsKey("session_id")){
                    Log.e(TAG, "Error on the server side: there are no session_id returned: " + response.data.keySet());
                }
                else{
                    Log.i(TAG, "User attempt to connect with wrong credential");
                }

            }
            //catch (retrofit.RetrofitError ex){
            //    Toast.makeText(getApplicationContext(), R.string.error_server_unavailable, Toast.LENGTH_LONG).show();
             //   Log.e(TAG, "Server unavailable");
            //}
            catch (Exception ex){
                Toast.makeText(getApplicationContext(), R.string.error_server_unavailable, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Exception: " + ex);
            }
            return false;
        }

        /*@Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            //showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //showProgress(false);
        }*/
    }

    private void setProgressVisibility(boolean bool) {
        if(bool) {
            progressView.setVisibility(View.VISIBLE);
            layoutFb.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            layoutFb.setVisibility(View.VISIBLE);
        }
    }
}

