package com.timappweb.timapp.rest.callbacks;

import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.User;

/**
 * Created by stephane on 6/7/2016.
 */
public class PublishInEventCallback extends HttpCallback {

    private final int actionType;

    public PublishInEventCallback(Event event, User user, int actionType) {
        this.actionType = actionType;
    }

    @Override
    public void successful(Object feedback) {
        QuotaManager.instance().add(actionType);
    }

    @Override
    public void forbidden() {
        // TODO set quota..

        // Above quota ??? TODO
    }
}