package com.timappweb.timapp.cache;

import com.timappweb.timapp.data.LocalPersistenceManager;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.entities.UsersPlace;
import com.timappweb.timapp.utils.Util;

import java.util.HashMap;

/**
 * Created by stephane on 1/29/2016.
 */
public class CacheData {

    private static final String KEY_LAST_PLACE = "cachedata_lastplace";
    private static final String KEY_LAST_POST = "cachedata_lastpost";
    private static final String KEY_MAP_USER_PLACES = "cachedata_map_user_places";

    public static HashMap<Integer, UsersPlace> mapPlaceStatus = new HashMap<>();
    private static Place lastPlace = null;
    private static Post lastPost = null;



    public static void load(){
        lastPlace = LocalPersistenceManager.readObject(CacheData.KEY_LAST_PLACE, Place.class);
        lastPost = LocalPersistenceManager.readObject(CacheData.KEY_LAST_POST, Post.class);
        mapPlaceStatus = LocalPersistenceManager.readObject(CacheData.KEY_MAP_USER_PLACES, HashMap.class);
    }

    public static void setLastPlace(Place lastPlace) {
        CacheData.lastPlace = lastPlace;
        LocalPersistenceManager.writeObject(CacheData.KEY_LAST_PLACE, lastPlace);
    }

    public static void setLastPost(Post lastPost) {
        CacheData.lastPost = lastPost;
        LocalPersistenceManager.writeObject(CacheData.KEY_LAST_POST, lastPost);
    }

    public static void addUserStatus(int placeId, UserPlaceStatus status){
        // TODO remove old ones
        UsersPlace userPlace = new UsersPlace();
        userPlace.place_id = placeId;
        userPlace.status = status;
        mapPlaceStatus.put(placeId, userPlace);
        LocalPersistenceManager.writeObject(KEY_MAP_USER_PLACES, mapPlaceStatus);
    }

    // Last post
    public static boolean isAllowedToAddPlace(){
        return lastPlace == null || Util.isOlderThan(lastPlace.created, 60 );
    }
    public static boolean isAllowedToAddPost(){
        return lastPost == null || Util.isOlderThan(lastPost.created, 60 );
    }
    // Last place status
    public static boolean isAllowedToAddUserStatus(int placeId, UserPlaceStatus status){
        if (mapPlaceStatus.containsKey(placeId)){
            UsersPlace placeStatus = mapPlaceStatus.get(placeId);
            // If there is already a user status
            if (placeStatus.status == status && !Util.isOlderThan(placeStatus.created, 60) ){
                return true;
            }
        }
        return false;
    }


}
