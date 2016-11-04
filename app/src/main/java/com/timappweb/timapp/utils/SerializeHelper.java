package com.timappweb.timapp.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.data.models.MyModel;

/**
 * Created by stephane on 6/2/2016.
 */
public class SerializeHelper {

    private static Gson serializer = new GsonBuilder()
            // @warning When using Gson with ActiveAndroid model, it will crash if you remove this line...
            //.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static <T extends MyModel> String packModel(T obj, Class<T> type){
        return serializer.toJson(obj, type);
    }

    public static <T> String pack(T obj){
        return serializer.toJson(obj);
    }

    public static <T> T unpack(String data, Class<T> classOfT) {
        return serializer.fromJson(data, classOfT);
    }

    /**
     * See http://stackoverflow.com/questions/18397342/deserializing-generic-types-with-gson
     * @param data
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T extends MyModel> T unpackModel(String data, Class<T> classOfT) {
        return serializer.fromJson(data, classOfT);
    }

}
