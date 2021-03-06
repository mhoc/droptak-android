package com.droptak.android.fragments;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.droptak.android.fragments.dialogs.CreateTakDialog;
import com.droptak.android.interfaces.OnLocationReadyListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import com.droptak.android.activities.MainActivity;
import com.droptak.android.data.MapID;
import com.droptak.android.data.MapObject;
import com.droptak.android.data.MapTakDB;
import com.droptak.android.data.TakObject;
import com.droptak.android.interfaces.OnGMapLoadedListener;
import com.droptak.android.managers.UserLocationManager;

/** To create instances of this class, you can do a standard "new" thing but
 *  it won't have any points on it. To get points on it, create the class with
 *  a call to "newInstanceOf()" and pass in the MapObject you want to display. */
public class TakMapFragment extends MapFragment {

    /** Listener for when the gmap has been fully loaded to the screen */
    private OnGMapLoadedListener loadedListener;
    private boolean animateCamera;


    public TakMapFragment() {
        this.animateCamera = false;
    }

    public TakMapFragment(boolean anim) {
        this.animateCamera = anim;
    }

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

            if (mo.getTaks().size() == 0) {
                centerCameraOnUser();
            } else {
                addTaksToGMap(mo);
            }
        }

        // Set an on long press listener to add a tak as a specific location
        getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng latLng) {
                // Create the add tak fragment
                CreateTakDialog.newInstanceOf(latLng).show(getFragmentManager(), "create-tak-dialog-user-selected-location");
            }
        });

        // Alert listeners that the gmap is loaded
        if (loadedListener != null) {
            loadedListener.onGMapLoaded();
        }
    }

    /** Centers the map's camera on the user */
    public void centerCameraOnUser() {

        // Get their current location
        final UserLocationManager manager = new UserLocationManager(getActivity());
        manager.setOnLocationReadyListener(new OnLocationReadyListener() {
            public void onLocationReady() {
                double lat = manager.getLat();
                double lng = manager.getLng();
                LatLng userLatLng = new LatLng(lat, lng);

                // Center the map to that position
                CameraUpdate moveCam = CameraUpdateFactory.newLatLngZoom(userLatLng, 14.5f);
                if (getMap() != null) {
                    getMap().moveCamera(moveCam);
                }
            }
        });
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
        CameraUpdate camUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), p.x, p.y, 300);

        if (animateCamera) {
            gmap.animateCamera(camUpdate);
            //zoomIn();
        } else {
            gmap.moveCamera(camUpdate);
            //zoomIn();
        }

    }

    /** Sets the fragments onGmapLoadedListener for when the googlemap is fully loaded */
    public void setOnGMapLoadedListener(OnGMapLoadedListener listener) {
        this.loadedListener = listener;
    }

    /** Animates zooming in on the current point. A lot smoother and cooler than the stock
     *  Google zoom. */
    private void zoomIn() {

        float zoomLevel = getMap().getCameraPosition().zoom;
        float targetZoomLevel = 14;

        long delay = 0;
        final float zoomStep = 0.05f;

        while ((zoomLevel += zoomStep) < targetZoomLevel) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    getMap().moveCamera(CameraUpdateFactory.zoomBy(zoomStep));
                }
            }, delay);

            delay += 10;
        }
    }

}
