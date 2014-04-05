package edu.purdue.maptak.admin.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class AddMapSelectionDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private String[] listItems = new String[] {
            "Create A Map",
            "Scan A QR Code",
            "Search"
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Add A Map...");
        builder.setItems(listItems, this);

        return builder.create();

    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

        switch (i) {
            case 0:
                // Create a new map
                CreateMapDialog d = new CreateMapDialog();
                d.show(getFragmentManager(), "create_map_dialog");
                break;
            case 1:

                break;
            case 2:

                break;
        }

    }
}
