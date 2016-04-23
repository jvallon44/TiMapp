package com.timappweb.timapp.data.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/4/2016.
 */
@Table(name = "activities")
public class UserActivity extends  BaseModel{

    @Column(name = "DateCreated", index = true)
    public int created;


    @Column(name = "QuotaType")
    public QuotaType type;


    public UserActivity() {
        super();
    }

    public UserActivity(String type) {
        super();
        this.type = QuotaType.getByType(type);
        this.created = Util.getCurrentTimeSec();
    }
}