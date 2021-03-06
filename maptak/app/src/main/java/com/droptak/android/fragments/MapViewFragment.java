package com.droptak.android.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import com.droptak.android.R;
import com.droptak.android.activities.MainActivity;
import com.droptak.android.data.MapID;
import com.droptak.android.data.MapTakDB;
import com.droptak.android.data.TakObject;
import com.droptak.android.fragments.dialogs.CreateTakDialog;
import com.droptak.android.fragments.dialogs.TakListDialog;
import com.droptak.android.interfaces.OnLocationReadyListener;
import com.droptak.android.managers.UserLocationManager;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

public class MapViewFragment extends Fragment implements View.OnClickListener {

    private boolean animateCamera;
    private TakMapFragment mapFragment;

    public MapViewFragment() {
        animateCamera = false;
    }

    public MapViewFragment(boolean animate) {
        this.animateCamera = animate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_takmapview, container, false);

        // Prepare buttons on screen
        Button buAddTak = (Button) v.findViewById(R.id.takmapview_bu_addtak);
        Button buTakList = (Button) v.findViewById(R.id.takmapview_bu_taklist);
        buAddTak.setOnClickListener(this);
        buTakList.setOnClickListener(this);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add the google map to the screen
        // This is done here because layout_mainview isn't inflated until after onCreateView
        mapFragment = new TakMapFragment(animateCamera);
        getFragmentManager().beginTransaction().replace(R.id.takmapview_layout_mainview, mapFragment).commit();
    }

    @Override
    public void onClick(View view) {

        // Get shared prefs and database
        MapTakDB db = MapTakDB.getDB(getActivity());
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);

        // Get the current map ID
        MapID id = new MapID(prefs.getString(MainActivity.PREF_CURRENT_MAP, ""));

        switch (view.getId()) {
            case R.id.takmapview_bu_addtak:

                // Get the user's location
                final UserLocationManager manager = new UserLocationManager(getActivity());
                manager.setOnLocationReadyListener(new OnLocationReadyListener() {
                    public void onLocationReady() {
                        // Get location
                        LatLng loc = new LatLng(manager.getLat(), manager.getLng());

                        // Show the dialog box for creating a new tak
                        CreateTakDialog.newInstanceOf(loc).show(getFragmentManager(), "create_tak_dialog");
                    }
                });



                break;

            case R.id.takmapview_bu_taklist:

                // Get the list of taks
                List<TakObject> taks = db.getTaks(id);

                // Create the dialogfragment
                new TakListDialog(taks).show(getFragmentManager(), "tak_list_dialog");

                break;
        }

    }
}
