package edu.purdue.maptak.admin;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

import edu.purdue.maptak.admin.data.MapTakDB;
import edu.purdue.maptak.admin.data.TakObject;
import edu.purdue.maptak.admin.test.DummyData;

public class TakListFragment extends ListFragment {

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_taklist, container, false);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //MapTakDB db = new MapTakDB();
        //List<TakObject> taks = db.getTaks(MainActivity.currentlySelectedMap);

        //Placeholder dummy data
        List<String> taks = new LinkedList<String>();
        taks.add(DummyData.createDummyTakObject().getLabel());
        taks.add(DummyData.createDummyTakObject().getLabel());
        taks.add(DummyData.createDummyTakObject().getLabel());
        taks.add(DummyData.createDummyTakObject().getLabel());


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, taks);
        setListAdapter(adapter);
    }

    @Override public void onListItemClick(ListView l, View v, int position, long id) {
        // do something with the data
    }

    // Container Activity must implement this interface
    public interface OnTakSelectedListener {
        public void onTakSelected();
    }
}