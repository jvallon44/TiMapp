package com.timappweb.timapp.activities;

import android.content.Intent;
import android.location.Location;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.models.dummy.DummyEventFactory;
import com.timappweb.timapp.data.models.dummy.DummySpotFactory;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.DisableQuotaRequestInterceptor;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.mocklocations.AbstractMockLocationProvider;
import com.timappweb.timapp.utils.viewinteraction.AddEventForm;
import com.timappweb.timapp.utils.viewinteraction.AddSpotForm;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by Stephane on 17/08/2016.
 *
 * TODO: disable quota on server and local side for tests to work properly
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEventActivityTest extends AbstractActivityTest {

    private static final String TAG = "AddEventActivityTest";


    @Rule
    public ActivityTestRule<AddEventActivity> mActivityRule = new ActivityTestRule<>(
            AddEventActivity.class, false, false);

    @Before
    public void setUp() throws Exception {
        this.systemAnimations(false);
        this.idlingApiCall();

        super.beforeTest();

        mActivityRule.launchActivity(new Intent(MyApplication.getApplicationBaseContext(), AddSpotActivity.class));

        this.getMockLocationProvider().route(new AbstractMockLocationProvider.MockLocationRoute() {
            @Override
            public Location getNextLocation() {
                Log.v(TAG, "Creating new mock location from route");
                return AbstractMockLocationProvider.createMockLocation("MockedLocation", MockLocation.START_TEST.latitude, MockLocation.START_TEST.longitude);
            }
        }, 2000);

    }

    @After
    public void tearDown() {
        this.resetAsBeforeTest();
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void postNewEventNoSpot() {
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        this.disableQuota();
        this.waitForFineLocation();

        String eventName = DummyEventFactory.uniqName();
        String eventDescription = "This is the big description for my event!";

        new AddEventForm()
                .tryAll(eventName, eventDescription)
                .submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    @CreateConfigAction
    @CreateAuthAction
    @Ignore
    public void postNewEventWithPicture() {
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        this.disableQuota();
        this.waitForFineLocation();

        String eventName = DummyEventFactory.uniqName();
        String eventDescription = "This is the big description for my event!";

        new AddEventForm()
                .tryAll(eventName, eventDescription)
                .submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }


    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void postNewEventWithExistingSpot() throws InterruptedException {
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        this.disableQuota();
        this.waitForFineLocation();

        String eventName = DummyEventFactory.uniqName();
        String eventDescription = "This is the big description for my event!";

        AddEventForm addEventForm = new AddEventForm()
                .tryAll(eventName, eventDescription)
                .openSpotActivity();

        new AddSpotForm()
                .waitForExistingSpotLoad()
                .selectExistingSpot(0);

        addEventForm.submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void postNewEventWithNewSpot() {
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        this.disableQuota();
        this.waitForFineLocation();

        String eventName = DummyEventFactory.uniqName();
        String eventDescription = "This is the big description for my event!";
        String spotName = DummySpotFactory.uniqName();

        AddEventForm addEventForm = new AddEventForm()
                .tryAll(eventName, eventDescription)
                .openSpotActivity();

        new AddSpotForm()
                .tryAll(spotName)
                .submit();

        ActivityHelper.assertCurrentActivity(AddEventActivity.class);

        addEventForm
                .submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }


    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void postValidationErrors() {
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        String wrongName = "O";

        new AddEventForm()
                .setName(wrongName)
                .submit();

        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
    }

    /***
     * Test that we can edit a spot newly created
     */
    @Test
    @CreateConfigAction
    @CreateAuthAction
    @Ignore
    public void reditCreatedSpot() {
        String spotName = "My new spot unique";

        AddEventForm addEventForm = new AddEventForm()
                .openSpotActivity();

        AddSpotForm addSpotForm = new AddSpotForm()
                .tryAll(spotName)
                .submit();

        addEventForm.editSpot(); // TODO Manage click on ContextMenu -_-

        addSpotForm.checkNameEquals(spotName);
        // TODO check category too
    }

    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void postExistingEvent() {
        this.disableQuota();
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        this.waitForFineLocation();
        String eventName = "Existing event";
        String eventDescription = "";

        AddEventForm addEventForm = new AddEventForm()
                .tryAll(eventName, eventDescription)
                .submit();

        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
        addEventForm.assertNameError();
    }


    // TODO
    @Ignore
    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void noValidGpsLocation() {

    }
    // ---------------------------------------------------------------------------------------------

    private void disableQuota() {
        QuotaManager.clear();
        RestClient.instance().getHttpBuilder()
                .addInterceptor(new DisableQuotaRequestInterceptor());
        RestClient.instance().buildAll();
    }

    private void waitForFineLocation() {
        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
        assertNotNull(mActivityRule.getActivity());
        if (mActivityRule.getActivity().getFineLocation() == null) {
            Log.w(TAG, "Start waiting for fine location");
            while  (mActivityRule.getActivity().getFineLocation() == null){
                TestUtil.sleep(100);
            }
        }
    }
}
