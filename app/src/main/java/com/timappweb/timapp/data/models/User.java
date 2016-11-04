package com.timappweb.timapp.data.models;

import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.sync.data.DataSyncAdapter;

import java.util.List;

@Table(database = AppDatabase.class)
public class User extends SyncBaseModel  {
    private static final String TAG = "UserEntity" ;

    // =============================================================================================
    // DATABASE

    @Column
    @NotNull
    @Expose
    public String username;

    @Column
    @Expose
    public String email;

    @Column
    @Expose(serialize = false, deserialize = true)
    public Integer count_posts;

    @Column
    @Expose(serialize = false, deserialize = true)
    public Integer count_places;

    @Column
    @Expose
    public String avatar_url;

    //@Column
    //@Expose(serialize = false, deserialize = true)
    //private boolean status = false;

    @Column
    @Expose(serialize = false, deserialize = true)
    public String app_id;

    @Column
    @Expose(serialize = false, deserialize = true)
    public String google_messaging_token;

    // =============================================================================================

    /**
     * Cached value. See @getTags
     */
    @Expose(serialize = false, deserialize = true)
    @ModelAssociation(
            type = ModelAssociation.Type.BELONGS_TO_MANY,
            saveStrategy = ModelAssociation.SaveStrategy.REPLACE,
            joinModel = UserTag.class,
            targetModel = Tag.class,
            targetTable = UserTag_Table.class,
            remoteForeignKey = "user_id")
    public List<Tag> tags;

    /**
     * Cached value
     */
    protected List<UserEvent> placeStatus;

    // =============================================================================================

    public User(){

    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", avatar_url=" + avatar_url +
                ", app_id=" + app_id +
                ", google_messaging_token =" + google_messaging_token +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePictureUrl() {
        return this.avatar_url;
    }

    public boolean hasTags() {
        return getTags().size() > 0;
    }

    // =============================================================================================

    public List<Tag> getTags() {
        if (tags != null) return tags;
        tags = SQLite.select()
                .from(Tag.class)
                .innerJoin(UserTag.class)
                .on(UserTag_Table.tag_id.eq(Tag_Table.id))
                .where(UserTag_Table.user_id.eq(this.id))
                .queryList();
        return tags;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }

    @Override
    public int getSyncType() {
        return DataSyncAdapter.SYNC_TYPE_USER;
    }

    public List<UserEvent> getPlaceStatus() {
        if (placeStatus != null) return placeStatus;
        placeStatus = SQLite.select()
                .from(UserEvent.class)
                .where(UserEvent_Table.user_id.eq(this.id))
                .queryList();
        return placeStatus;
    }


    public Where<EventsInvitation> getInviteSentQuery() {
        return SQLite.select()
                .from(EventsInvitation.class)
                .where(EventsInvitation_Table.user_source_id.eq(this.id));
    }

    public List<EventsInvitation> getInviteSent(long placeId) {
        return this.getInviteSentQuery().queryList();
    }

    public Where<EventsInvitation> getInviteReceivedQuery() {
        return SQLite.select()
                .from(EventsInvitation.class)
                .where(EventsInvitation_Table.user_target_id.eq(this.id))
                .orderBy(EventsInvitation_Table.id, false);
    }

    public List<EventsInvitation> getInviteReceived() {
        return this.getInviteReceivedQuery().queryList();
    }


    public UserQuota getQuota(int quotaTypeId) {
        return UserQuota.get(this.id, quotaTypeId);
    }


    @Override
    public void deepSave() throws CannotSaveModelException {
        if (this.tags != null && tags.size() > 0){
            // TODO save tags
        }
        this.mySave();
    }
}
