package com.timappweb.timapp.entities;

/**
 * Created by stephane on 2/18/2016.
 */
public class Picture {

    public int id;
    public String photo;
    public String photo_dir;
    public int place_id;
    public int user_id;

    public String getUrl(){
        return this.photo_dir + this.photo;
    }
}