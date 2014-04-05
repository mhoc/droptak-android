package edu.purdue.maptak.admin.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.io.File;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import edu.purdue.maptak.admin.R;
import edu.purdue.maptak.admin.data.MapID;
import edu.purdue.maptak.admin.data.MapObject;
import edu.purdue.maptak.admin.data.MapTakDB;
import edu.purdue.maptak.admin.data.TakObject;
import edu.purdue.maptak.admin.tasks.AddMapTask;


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
    private void createMap(String name, boolean isPrivate) {

        // Create the task that we use to push the map to the server
        AddMapTask addMapTask = new AddMapTask(name, getActivity());

        // Get the new ID from the JSON returned by the server
        MapID realMapID = null;
        try {
            String jsonString = addMapTask.execute().get();
            JSONObject jsonObject = new JSONObject(jsonString);
            String realMapIDString = jsonObject.getString("mapId");
            realMapID = new MapID(realMapIDString);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create the map object based on the information the user has given us
        LinkedList<TakObject> taks = new LinkedList<TakObject>();
        MapObject mapObject = new MapObject(name, realMapID, taks, isPrivate);

        // Add the new map to the local database
        MapTakDB db = MapTakDB.getDB(getActivity());
        db.addMap(mapObject);

        // Close the dialog
        getDialog().cancel();
    }

    /** Saved this code from kyle's implementation of this functionality */
    private void backupDatabase() throws FileNotFoundException, IOException {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        if (sd.canWrite()) {
            String currentDBPath = "//data//"+ "edu.purdue.maptak.admin" +"//databases//"+"database_cached_taks";
            String backupDBPath = "database_cahced_taks";
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            // Toast.makeText(, backupDB.toString(), Toast.LENGTH_LONG).show();
            Log.d("debug","sql backupmade");

        }
    }

}
