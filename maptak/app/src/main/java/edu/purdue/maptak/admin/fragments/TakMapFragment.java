package edu.purdue.maptak.admin.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.purdue.maptak.admin.R;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.purdue.maptak.admin.activities.MainActivity;
import edu.purdue.maptak.admin.data.MapID;
import edu.purdue.maptak.admin.data.MapObject;
import edu.purdue.maptak.admin.data.MapTakDB;
import edu.purdue.maptak.admin.data.TakObject;
import edu.purdue.maptak.admin.interfaces.OnGMapLoadedListener;
import edu.purdue.maptak.admin.managers.UserLocationManager;

/** To create instances of this class, you can do a standard "new" thing but
 *  it won't have any points on it. To get points on it, create the class with
 *  a call to "newInstanceOf()" and pass in the MapObject you want to display. */
public class TakMapFragment extends MapFragment {

    /** Listener for when the gmap has been fully loaded to the screen */
    private OnGMapLoadedListener loadedListener;

    /** Called when the fragment has been fully inflated into the activity */
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Enable location tracking
        getMap().setMyLocationEnabled(true);

        // Get the currently loaded map object if it exists
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        String mapIDStr = prefs.getString(MainActivity.PREF_CURRENT_MAP, "");

        // Add the pins to the map
        if (mapIDStr.equals("")) {
            centerCameraOnUser();
        } else {
            MapID mapID = new MapID(mapIDStr);
            MapObject mo = MapTakDB.getDB(getActivity()).getMap(mapID);
            addTaksToGMap(mo);
        }

        // Alert listeners that the gmap is loaded
        if (loadedListener != null) {
            loadedListener.onGMapLoaded();
        }
    }

    /** Centers the map's camera on the user */
    public void centerCameraOnUser() {

        // Get their current location
        UserLocationManager manager = new UserLocationManager(getActivity());

        if (manager.isLocationAvailable()) {
            double lat = manager.getLat();
            double lng = manager.getLng();
            LatLng userLatLng = new LatLng(lat, lng);

            // Center the map to that position
            CameraUpdate moveCam = CameraUpdateFactory.newLatLngZoom(userLatLng, 14.5f);
            getMap().moveCamera(moveCam);
        }
    }

    /** Clears all of the current pins off this fragments google map, adds all the pins for a
     *  given MapObject, then zooms the camera to encompass all of these points. This is done
     *  without any database transactions. */
    public void addTaksToGMap(MapObject map) {

        // Get the gmap on which we will draw the points
        GoogleMap gmap = getMap();
        gmap.clear();

        // If the map has no points (brand new) then just return
        if (map.getTaks().size() == 0) {
            return;
        }

        // Get all the latlng points for the map and add them
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (TakObject t : map.getTaks()) {
            LatLng l = new LatLng(t.getLat(), t.getLng());
            builder.include(l);
            gmap.addMarker(new MarkerOptions()
                    .title(t.getName())
                    .position(l));
        }

        // Animate the camera to include the points we added
        Point p = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(p);
        gmap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), p.x, p.y, 200));
    }

    /** Sets the fragments onGmapLoadedListener for when the googlemap is fully loaded */
    public void setOnGMapLoadedListener(OnGMapLoadedListener listener) {
        this.loadedListener = listener;
    }

}
