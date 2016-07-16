package com.timappweb.timapp.utils.location;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.fragments.ExploreMapFragment;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 5/16/2016.
 */
public class LocationManager {

    private static final String TAG = "LocationManager";

    // =============================================================================================

    private static Location                 lastLocation        = null;
    private static com.google.android.gms.location.LocationListener     mLocationListener   = null;
    private static MyLocationProvider       locationProvider    = null;
    private static List<LocationListener>   listeners           = new LinkedList<>();;

    // =============================================================================================

    public static void setLastLocation(Location l) {
        Log.i(TAG, "Location has changed: " + l.toString());
        lastLocation = l;
    }

    /**
     * Check if there is a last location that is not outdated
     * @return
     */
    public static boolean hasLastLocation() {
        return lastLocation != null &&
                (lastLocation.getTime() - System.currentTimeMillis()) < ConfigurationProvider.rules().gps_min_time_delay;
    }

    /**
     * Check if there is a last location with a fine location
     * @return
     */
    public static boolean hasFineLocation() {
        return hasFineLocation(ConfigurationProvider.rules().gps_min_accuracy);
    }

    public static boolean hasFineLocation(int minAccuracy) {
        return hasLastLocation() &&
                lastLocation.getAccuracy() <= minAccuracy;
    }

    public static Location getLastLocation() {
        return lastLocation;
    }


    private static void initLocationListener(){
        mLocationListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Location tmpLocation = lastLocation;
                setLastLocation(location);
                for (LocationListener listener : listeners){
                    listener.onLocationChanged(location, tmpLocation);
                }
            }
        };
    }


    public static void initLocationProvider(Activity activity) {
        locationProvider = new MyLocationProvider(activity, mLocationListener);

        if (!locationProvider.isGPSEnabled()){
            locationProvider.askUserToEnableGPS();
        }

        locationProvider.requestMultipleUpdates();
    }

    public static void addOnLocationChangedListener(LocationListener locationListener) {
        if (!listeners.contains(locationListener)){
            listeners.add(locationListener);
        }
    }


    public static void start(Activity activity){
        Log.v(TAG, "Starting location manager");
        initLocationListener();
        initLocationProvider(activity);
        locationProvider.connect();
    }

    public static void stop(){
        Log.v(TAG, "Stopping location manager");
        listeners.clear();
        if (locationProvider != null) {
            locationProvider.disconnect();
        }

    }

    public static LatLngBounds generateBoundsAroundLocation(Location location, int size) {
        double offsetLatitude = DistanceHelper.metersToLatitude(size) / 2;
        double offsetLongitude = DistanceHelper.metersToLongitude(size, location.getLatitude()) / 2;
        return new LatLngBounds(
                new LatLng(location.getLatitude() - offsetLatitude, location.getLongitude() - offsetLongitude),
                new LatLng(location.getLatitude() + offsetLatitude, location.getLongitude() + offsetLongitude));
    }

    public static void removeLocationListener(Object object) {
        listeners.remove(object);
    }

    // =============================================================================================

    public interface LocationListener{
        void onLocationChanged(Location newLocation, Location lastLocation);
    }

}
