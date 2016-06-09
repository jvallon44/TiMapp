package com.timappweb.timapp.rest.io.serializers;

import com.google.gson.JsonObject;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.exceptions.UnknownCategoryException;

/**
 * Created by stephane on 6/8/2016.
 */
public class AddEventMapper {

    public static JsonObject toJson(Event event){
        JsonObject jsonObject = new JsonObject();
        if (event.hasSpot()){
            jsonObject.add("spot", AddSpotMapper.toJson(event.getSpot()));
        }
        jsonObject.addProperty("name", event.getName());
        jsonObject.addProperty("description", event.getDescription());
        jsonObject.addProperty("latitude", event.latitude);
        jsonObject.addProperty("longitude", event.longitude);
        try {
            if (event.hasCategory()){
                    jsonObject.addProperty("category_id",  event.getCategory().getRemoteId());
            }
        } catch (UnknownCategoryException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}