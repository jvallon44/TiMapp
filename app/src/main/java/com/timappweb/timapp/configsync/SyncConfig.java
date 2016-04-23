package com.timappweb.timapp.configsync;

import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/22/2016.
 */


public class SyncConfig<DataType>{

    @SerializedName("lastSync")
    protected long lastSync;

    @SerializedName("version")
    public int version;

    @SerializedName("data")
    public DataType data;

    @SerializedName("type")
    public String type;

    public SyncConfig() {
        this.version = 0;
        this.data = null;
        this.lastSync = 0;
    }

    @Override
    public String toString() {
        return "SyncConfig{" +
                "version=" + version +
                ", type=" + type +
                ", data=" + data +
                ", lastSync=" + lastSync + " ("+ Util.delayFromNow((int) (lastSync / 1000))+" seconds ago)" +
                '}';
    }
}