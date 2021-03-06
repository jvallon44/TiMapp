package com.timappweb.timapp.rest.callbacks;

import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.responses.ClientError;

/**
 * Created by stephane on 6/7/2016.
 */
public class PublishInEventCallback<T> extends HttpCallback<T> {

    private final int actionType;
    private final Event event;
    private final boolean updateEvent;

    public PublishInEventCallback(Event event, User user, int actionType) {
        this(event, user, actionType, true);
    }

    public PublishInEventCallback(Event event, User user) {
        this(event, user, -1);
    }

    public PublishInEventCallback(Event event, User user, int actionType, boolean b) {
        this.actionType = actionType;
        this.event = event;
        this.updateEvent = b;
    }

    @Override
    public void successful(T feedback) {
        if (actionType != -1) QuotaManager.instance().add(actionType);
        if (updateEvent){
            RestClient.buildCall(RestClient.service().updateEventInfo(event.getRemoteId(), event.picture != null ? event.picture.getRemoteId() : 0))
                    .onResponse(new UpdateEventCallback(event))
                    .perform();
        }
        if (actionType == QuotaType.ADD_PICTURE || actionType == QuotaType.ADD_TAGS){
            EventStatusManager.setCurrentEvent(event);
        }
    }


    @Override
    public void forbidden(ClientError clientError) {
        // TODO set quota..
    }
}
