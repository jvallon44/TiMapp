package com.timappweb.timapp.utils;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.timappweb.timapp.configsync.SyncConfig;
import com.timappweb.timapp.rest.JsonConfDeserializer;

import java.lang.reflect.Modifier;

/**
 * Created by stephane on 6/2/2016.
 */
public class SerializeHelper {

    private static Gson serializer = new GsonBuilder()
            // @warning When using Gson with ActiveAndroid model, it will crash if you remove this line...
            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .create();

    public static String pack(Object obj){
        return serializer.toJson(obj);
    }

    public static <T> T unpack(String data, Class<T> classOfT) {
        return serializer.fromJson(data, classOfT);
    }
}
