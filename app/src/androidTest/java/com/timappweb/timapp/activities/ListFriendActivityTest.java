package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.viewinteraction.RecyclerViewHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.greaterThan;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ListFriendActivityTest extends AbstractActivityTest {

    @Rule
    public ActivityTestRule<ListFriendsActivity> mActivityRule = new ActivityTestRule<>(
            ListFriendsActivity.class, false, false);


    @Before
    public void setUp() throws Exception {
        this.idlingApiCall();
        this.systemAnimations(false);
        super.beforeTest();
        this.startActivity();
    }

    @After
    public void tearDown() throws Exception {
        this.resetAsBeforeTest();
    }

    public void startActivity(){
        Intent intent = new Intent(MyApplication.getApplicationBaseContext(), ListFriendsActivity.class);
        mActivityRule.launchActivity(intent);
    }

    // ---------------------------------------------------------------------------------------------

    @CreateConfigAction
    @CreateAuthAction
    @Test
    public void testViewUserProfile() {
        TestUtil.sleep(3000); // Tmp fix, while there are no loader on friends activity
        new RecyclerViewHelper(R.id.rv_friends)
                .checkItemCount(greaterThan(0))
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(ProfileActivity.class);
    }

    @CreateConfigAction
    @CreateAuthAction(payloadId = "132743310518589") //ID of User without friends
    @Test
    public void testNoDataView() {
        TestUtil.sleep(3000); // Tmp fix, while there are no loader on friends activity
        //TODO Refresh.
    }

}
