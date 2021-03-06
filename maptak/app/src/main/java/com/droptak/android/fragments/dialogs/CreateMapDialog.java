package com.droptak.android.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.droptak.android.R;
import com.droptak.android.activities.MainActivity;
import com.droptak.android.data.MapObject;
import com.droptak.android.data.User;
import com.droptak.android.fragments.DrawerFragment;
import com.droptak.android.fragments.MapViewFragment;
import com.droptak.android.tasks.CreateMapTask;


public class CreateMapDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private EditText etName;
    private Switch swIsPrivate;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the alert dialog title
        builder.setTitle("Create A New Map");

        // Set main content view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_createmap, null);
        builder.setView(v);

        // Set positive and negative buttons
        builder.setPositiveButton("Create", this);
        builder.setNegativeButton("Cancel", this);

        // Prepare the widgets on the screen
        etName = (EditText) v.findViewById(R.id.addmap_et_mapname);
        swIsPrivate = (Switch) v.findViewById(R.id.addmap_sw_isprivate);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int id) {

        // Do something whether the user has clicked "create" or "exit"
        switch (id) {
            case DialogInterface.BUTTON_POSITIVE:
                String name = etName.getText().toString();
                boolean isPrivate = swIsPrivate.isChecked();
                createMap(name, isPrivate);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                getDialog().cancel();
                break;
        }

    }

    /** Creates the map and pushes it to the database using a background task */
    private void createMap(String mapName, boolean isPrivate) {

        // Get the shared preferences
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        String uid = prefs.getString(MainActivity.PREF_USER_MAPTAK_TOKEN, "");
        String userName = prefs.getString(MainActivity.PREF_USER_GPLUS_NAME, "");
        String userEmail = prefs.getString(MainActivity.PREF_USER_GPLUS_EMAIL, "");

        // Create a filler map object that will hold all the information pushed to the server
        MapObject map = new MapObject();
        map.setName(mapName);
        map.setOwner(new User(uid, userName, userEmail));

        // TODO: Properly set if it is public or not
        map.setIsPublic(!isPrivate);

        // Create the drawerfragment that will be inflated later
        DrawerFragment drawerF = new DrawerFragment();

        // Create and execute the task which adds the map to the database and the server
        CreateMapTask task = new CreateMapTask(getActivity(), map, drawerF);
        task.execute();

        // Inflate the map as the current selected map
        // Note that the ID field is set (to a temporary value) in task.execute(), so this is alright to run.
        prefs.edit().putString(MainActivity.PREF_CURRENT_MAP, map.getID().toString()).commit();
        getFragmentManager().beginTransaction().replace(R.id.mainview, new MapViewFragment(true)).commit();

        // Close the dialog and re-inflate the side drawer to refresh the map list
        getFragmentManager().beginTransaction().replace(R.id.left_drawer, drawerF).commit();
        getDialog().cancel();
    }

}
