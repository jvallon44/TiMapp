package com.timappweb.timapp.data.entities;

import com.google.gson.annotations.Expose;
import com.timappweb.timapp.adapters.EventUsersAdapter;
import com.timappweb.timapp.data.models.Post;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class User implements Serializable, PlaceUserInterface {
    private static final String TAG = "UserEntity" ;

    @Expose
    public int id = -1;

    @Expose
    public String username;

    @Expose(serialize = true, deserialize = false)
    public String password;

    @Expose
    public String email;

    @Expose(serialize = false, deserialize = true)
    public int count_posts = 0;

    @Expose(serialize = false, deserialize = true)
    public int count_places = 0;

    @Expose
    public SocialProvider provider;

    @Expose
    public String provider_uid;

    @Expose(serialize = false, deserialize = true)
    private boolean status = false;

    @Expose(serialize = false, deserialize = true)
    public LinkedList<Post> posts;

    @Expose(serialize = false, deserialize = true)
    public List<Tag> tags;

    @Expose(serialize = false, deserialize = true)
    public String app_id;

    @Expose(serialize = false, deserialize = true)
    public String google_messaging_token;

    public User(){

    }

    public User(String email, String password){
        this.email = email;
        this.password = password;
    }
    public User(String email, String password, String username){
        this.email = email;
        this.password = password;
        this.username = username;
    }

    private static int dummyIndice = 0;
    public static User createDummy(){
        User user = new User("user"+dummyIndice+"@dummy.com", "dummy", "Dummy User " + dummyIndice);
        dummyIndice++;
        return user;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", id=" + id +
                ", provider_uid=" + provider +
                ", app_id=" + app_id +
                ", google_messaging_token =" + google_messaging_token +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePictureUrl() {
        return "https://graph.facebook.com/" + this.provider_uid + "/picture?type=large";
    }

    @Override
    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public String getTimeCreated() {
        return null;
    }

    @Override
    public User getUser() {
        return this;
    }

    @Override
    public int getViewType() {
        return EventUsersAdapter.VIEW_TYPES.UNDEFINED;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    public String getTagsToString() {
        return Tag.tagsToString(this.tags);
    }

    public List<Tag> setNewbieTag() {
        return null;
    }
}