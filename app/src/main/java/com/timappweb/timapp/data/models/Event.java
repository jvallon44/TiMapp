package com.timappweb.timapp.data.models;

import android.content.Context;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.BR;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.entities.MarkerValueInterface;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "Event")
public class Event extends SyncBaseModel implements MarkerValueInterface {

    private static final String TAG = "PlaceEntity" ;
    private static final String REST_URL = "places";

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = Spot.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Spot", onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    @SerializedName("spot")
    @Expose
    public Spot             spot;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "User", onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    @SerializedName("user")
    @Expose(serialize = false, deserialize = true)
    public User             user;

    @Column(name = "Name")
    @Expose
    public String           name;

    @Column(name = "Description")
    @Expose
    public String           description;

    @Column(name = "Created")
    @Expose(serialize = false, deserialize = true)
    public int              created;

    @Column(name = "Latitude")
    @Expose
    public double           latitude;

    @Column(name = "Longitude")
    @Expose
    public double           longitude;

    @ModelAssociation(joinModel = EventCategory.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Category", notNull = false, onDelete = Column.ForeignKeyAction.SET_NULL, onUpdate = Column.ForeignKeyAction.SET_NULL)
    @Expose
    public EventCategory    event_category;

    @Column(name = "Points")
    @Expose(serialize = false, deserialize = true)
    public int              points;

    @Column(name = "CountHere")
    @Expose(serialize = false, deserialize = true)
    public Integer count_here;

    @Column(name = "CountComing")
    @Expose(serialize = false, deserialize = true)
    public Integer count_coming;

    // =============================================================================================
    // Fields

    @Expose(serialize = false, deserialize = true)
    public int              count_posts;

    @Expose(serialize = false, deserialize = false)
    public int              loaded_time = -1;

    @Expose(serialize = false, deserialize = true)
    public List<Tag>        tags;

    @Expose(serialize = false, deserialize = true)
    public ArrayList<Post>  posts;

    @Expose
    public Integer          spot_id  = null;

    public double           distance = -1;

    @Expose
    public int              category_id;

    // =============================================================================================

    public Event(){
        this.loaded_time = Util.getCurrentTimeSec();
    }

    public Event(int id, double lat, double lng, String name) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.remote_id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.count_posts = 0;
        this.posts = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Event(Location location, String name, EventCategory eventCategory, Spot spot, String description) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.setLocation(location);
        this.name = name;
        this.category_id = eventCategory.remote_id;
        this.description = description;
        if (spot != null){
            this.spot = spot;
            this.spot_id = spot.remote_id;
        }
    }

    public void addPost(Post post){
        this.count_posts++;
        this.posts.add(post);
    }

    /**
     * Return true if this event is not saved on the server
     * @return
     */
    public boolean isNew(){
        return this.remote_id == null;
    }

    public int countPosts(){
        return this.count_posts;
    }

    // Dummy data
    private static int dummyIndice = 0;

    public static Event createDummy(){
        Event event = new Event(1, dummyIndice, dummyIndice, "Test");
        event.tags.add(Tag.createDummy());
        event.addPost(Post.createDummy());
        event.addPost(Post.createDummy());
        event.addPost(Post.createDummy());
        event.addPost(Post.createDummy());
        dummyIndice++;
        return event;
    }

    // =============================================================================================

    @Override
    public LatLng getPosition() {
        return new LatLng(this.latitude, this.longitude);
    }


    public String getTime() {
        if (this.posts != null && posts.size() > 0){
            return posts.get(posts.size()-1).getPrettyTimeCreated();
        }
        return this.getPrettyTimeCreated();
    }

    /**
     * Get the created as a pretty time format
     *
     * @return
     */
    public String getPrettyTimeCreated() {
        PrettyTime p = new PrettyTime();
        return p.format(new Date(((long) this.created) * 1000));
    }

    @Override
    public String toString() {
        return "Event{" +
                "db_id=" + this.getId() +
                ", remote_id=" + remote_id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", created=" + created +
                ", longitude=" + longitude +
                ", category_id=" + category_id +
                ", spot=" + (spot != null ? spot : "No spot") +
                '}';
    }

    public int getPoints() {
        int points = this.points - (Util.getCurrentTimeSec() - this.loaded_time);
        return points > 0 ? points : 0;
    }

    public String displayPoints() {
        int points = this.points - (Util.getCurrentTimeSec() - this.loaded_time);
        return points > 0 ? String.valueOf(points) : "0";
    }

    public String displayCountComing() {
        return count_coming != null ? count_coming.toString() : "0";
    }
    public String displayCountHere() {
        return count_here != null ? count_here.toString() : "0";
    }

    public int getCategoryId() {
        return category_id;
    }

    public int getLevel(){
        return Event.computeLevel(this.getPoints());
    }

    public EventCategory getCategory() throws UnknownCategoryException {
        if (event_category == null){
            event_category = MyApplication.getCategoryById(this.category_id);
        }
        return event_category;
    }

    @Override
    public int getMarkerId() {
        return this.remote_id;
    }

    public boolean hasDistanceFromUser(){
        this.updateDistanceFromUser();
        return distance != -1;
    }

    public double getDistanceFromUser() {
        this.updateDistanceFromUser();
        return distance;
    }
    public String getAddress(){
        if (hasSpot()){
            return spot.getAddress();
        }
        else{
            return null;
        }
    }

    public MarkerOptions getMarkerOption() {
        return new MarkerOptions().position(getPosition());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Bindable
    public Spot getSpot() {
        return spot;
    }

    // =============================================================================================

    /**
     * @param latitude
     * @param longitude
     * @return true if the user can post in the event
     */
    public boolean isUserAround(double latitude, double longitude) {
        return DistanceHelper.distFrom(latitude, longitude, this.latitude, this.longitude)
                < ConfigurationProvider.rules().place_max_reachable;
    }

    public boolean isUserAround() {
        Location lastLocation = LocationManager.getLastLocation();
        if (lastLocation == null) return false;
        return this.isUserAround(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public static boolean isValidName(String name) {
        return name.trim().length() >= ConfigurationProvider.rules().places_min_name_length;
    }

    private static int computeLevel(int points) {
        List<Integer> levels = ConfigurationProvider.rules().places_points_levels;
        int num = 0;
        for (int level: levels){
            if (level >= points){
                return num;
            }
            num++;
        }
        return num;
    }

    // =============================================================================================

    @Override
    public boolean isSync(SyncBaseModel o) {
        if (o == null) return false;
        Event event = (Event) o;

        if (spot_id != event.spot_id) return false;
        if (created != event.created) return false;
        if (Double.compare(event.latitude, latitude) != 0) return false;
        if (Double.compare(event.longitude, longitude) != 0) return false;
        if (count_posts != event.count_posts) return false;
        if (category_id != event.category_id) return false;
        if (points != event.points) return false;
        if (!name.equals(event.name)) return false;
        return !(description != null ? !description.equals(event.description) : event.description != null);
    }



    // =============================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Event event = (Event) o;

        if (remote_id != event.remote_id) return false;
        if (spot_id != event.spot_id) return false;
        if (created != event.created) return false;
        if (Double.compare(event.latitude, latitude) != 0) return false;
        if (Double.compare(event.longitude, longitude) != 0) return false;
        if (count_posts != event.count_posts) return false;
        if (category_id != event.category_id) return false;
        if (points != event.points) return false;
        if (spot != null ? !spot.equals(event.spot) : event.spot != null) return false;
        if (!name.equals(event.name)) return false;
        return !(description != null ? !description.equals(event.description) : event.description != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + remote_id;
        return result;
    }


    public From getPicturesQuery() {
        return new Select().from(Picture.class).where("Event = ?", this.getId()).orderBy("created DESC");
    }

    public List<Picture> getPictures() {
        return getPicturesQuery().execute();
    }

    public List<? extends SyncBaseModel> getUsers() {
        return new Select().from(UserEvent.class).where("Event = ?", this.getId()).execute();
    }

    public From getTagsQuery() {
        return new Select()
                .from(EventTag.class)
                .where("EventTag.Event = ?", this.getId())
                //.join(EventTag.class).on("Tag.Id = EventTag.Tag")
                .orderBy("EventTag.CountRef DESC");
    }
    public void deleteTags() {
        new Delete().from(EventTag.class).where("EventTag.Event = ?", this.getId()).execute();
    }

    public List<EventTag> getTags() {
        return getTagsQuery().execute();
    }

    public boolean hasDescription() {
        return description != null && description.length() > 0;
    }

    public boolean hasSpot() {
        return spot != null;
    }

    public boolean hasTags() {
        return this.tags != null && tags.size() > 0;
    }

    public void updateDistanceFromUser() {
        Location location = LocationManager.getLastLocation();
        if (location == null)
            return;
        double distance =  DistanceHelper.distFrom(location.getLatitude(), location.getLongitude(),
                this.latitude, this.longitude);
        this.distance = Math.round(distance);
    }

    public String getName() {
        return name;
    }

    public EventCategory getCategoryWithDefault() {
        try {
            return this.getCategory();
        } catch (UnknownCategoryException e) {
            return new EventCategory("else");
        }
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
        notifyPropertyChanged(BR.spot);
    }

    public void setCategory(EventCategory category) {
        this.category_id = (int) category.getRemoteId();
    }

    public boolean isOver(){
        return this.getPoints() <= 0;
    }

    public void setLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public Drawable getBackgroundImage(Context context){
        return ContextCompat.getDrawable(context, getCategoryWithDefault().getBigImageResId());
    }

    public boolean hasAddress(){
        return getAddress() != null;
    }

    @Override
    public String getRestUrl() {
        return REST_URL;
    }

}
