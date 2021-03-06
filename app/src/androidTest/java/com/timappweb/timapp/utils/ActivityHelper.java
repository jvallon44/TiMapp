package com.timappweb.timapp.utils;

import android.app.Activity;
import android.support.test.espresso.Espresso;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import com.timappweb.timapp.R;

import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 07/09/2016.
 */
public class ActivityHelper {


    /**
     * Assert that current activity is activityClass
     * @param activityClass
     */
    public static void assertCurrentActivity(Class<? extends Activity> activityClass) {
        if (getActivityInstance() == null){
            TestUtil.sleep(100);
        }
        assertCurrentActivity(activityClass, getActivityInstance());
    }

    public static void assertCurrentActivity(Class<? extends Activity> activityClass, Activity currentActivity) {
        assertNotNull("You cannot pass a null argument to this function", activityClass);
        assertNotNull("Current activity is null", currentActivity);
        assertTrue("Current activity should be " + activityClass.getName() + " but is " + currentActivity.getClass().getName(), currentActivity.getClass().isAssignableFrom(activityClass));
    }

    public static Activity getActivityInstance(){
        final Activity[] activity = {null};
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()){
                    activity[0] = (Activity) resumedActivities.iterator().next();
                }
            }
        });

        return activity[0];
    }

    public static void btnClick(int id) {
        onView(withId(id))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(isClickable()))
                .perform(click());
    }

    public static void goBack() {
        Espresso.pressBack();
    }
}
