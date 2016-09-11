package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddSpotActivity;
import com.timappweb.timapp.utils.ActivityHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 07/09/2016.
 */
public class AddEventForm  extends FormHelper {

    private final ViewInteraction inputEventName;
    private final ViewInteraction inputEventDescription;
    private final ViewInteraction submitButton;
    private final CategorySelectorHelper categorySelector;
    private final ViewInteraction progressView;
    //private ProgressIdlingResource progressIdlingResource;

    public AddEventForm() {
        inputEventName = onView(withId(R.id.event_name));
        inputEventDescription = onView(withId(R.id.description_edit_text));
        submitButton = onView(withId(R.id.action_post));
        categorySelector = new CategorySelectorHelper();
        progressView = onView(withId(R.id.progress_view));
       //progressIdlingResource = null;
    }
/*
    public AddEventForm(ProgressIdlingResource progressIdlingResource) {
        this();
        /*
        this.progressIdlingResource = progressIdlingResource;
        progressIdlingResource.registerIdleTransitionCallback(new IdlingResource.ResourceCallback() {
            @Override
            public void onTransitionToIdle() {
                synchronized (AddEventForm.this){
                    AddEventForm.this.notify();
                }
            }
        });
    }
*/
    public AddEventForm setName(String value) {
        typeIn(inputEventName, value);
        return this;
    }

    public AddEventForm setDescription(String value) {
        typeIn(inputEventDescription, value);
        return this;
    }


    public AddEventForm setCategory(int position){
        categorySelector.selectByPosition(position);
        return this;
    }

    public AddEventForm submit() {
        this.assertSubmitEnabled();
        submitButton
                .perform(click());
        return this;
    }

    public AddEventForm addSpot() {
        onView(withId(R.id.button_add_spot))
                .perform(scrollTo())
                .perform(click());
        ActivityHelper.assertCurrentActivity(AddSpotActivity.class);
        return this;
    }

    public AddEventForm assertNameError() {
        this.assertValidationError(inputEventName);
        return this;
    }

    public AddEventForm assertLoader() {
        progressView
                .check(matches(isDisplayed()));
        return this;
    }

    public AddEventForm assertSubmitDisabled() {
        // TODO
        return this;
    }

    public AddEventForm assertSubmitEnabled() {
        submitButton
                .check(matches(isClickable()))
                .check(matches(isEnabled()));
        return this;
    }

    public AddEventForm editSpot() {
        onView(withId(R.id.spot_container))
                .perform(scrollTo())
                .perform(click());
        return this;
    }

    /*
    public AddEventForm waitLoadDone() throws InterruptedException {
        this.assertLoader();
        if (progressIdlingResource != null){
            synchronized (this){
                while (!progressIdlingResource.isIdleNow()) this.wait();
            }
        }
        return this;

    }*/
}
