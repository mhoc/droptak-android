package edu.purdue.maptak.admin.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import edu.purdue.maptak.admin.activities.MainActivity;
import edu.purdue.maptak.admin.R;
import edu.purdue.maptak.admin.managers.TakFragmentManager;
import edu.purdue.maptak.admin.data.MapObject;
import edu.purdue.maptak.admin.data.MapTakDB;

public class MapListFragment extends Fragment implements AdapterView.OnItemClickListener {

    /** List View that will display the maps currently accessible by the manager/user */
    ListView mapList;

    /** Used to access the MapTakDB class */
    MapTakDB mapTakDB;

    /** List Adapter that will hold the data that populates the list view */
    List<MapObject> backingMapList;
    ListAdapter listAdapter;

    /** Creates a new instance of a MapListFragment */
    public static MapListFragment newInstanceOf() {
        MapListFragment f = new MapListFragment();
        return f;
    }

    /** Inflates the view for this fragment. */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Create instance of database
        mapTakDB = MapTakDB.getDB(getActivity());

        // Inflate the view and create the listview
        View v = inflater.inflate(R.layout.fragment_maplist, container, false);
        mapList = (ListView) v.findViewById(R.id.fragment_maplist_listview);
        mapList.setOnItemClickListener(this);

        // Commit a database transaction to get the user's maps
        backingMapList = mapTakDB.getUsersMaps();

        // Set those maps as the list adapter for the list view and return the view
        listAdapter = new MapObjectAdapter(getActivity(), android.R.layout.simple_list_item_1, backingMapList);
        mapList.setAdapter(listAdapter);
        return v;
    }

    /** Custom ListView Adapter */
    private class MapObjectAdapter extends ArrayAdapter {
        private Context mContext;
        private int id;
        private List<MapObject> mMaps;

        public MapObjectAdapter(Context context, int textViewResourceId, List<MapObject> maps){
            super(context, textViewResourceId, maps);
            mContext = context;
            id = textViewResourceId;
            mMaps = maps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View row = convertView;
            MapObjectData temp = null;
            if ( row == null ){
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.listview_mapobject, parent, false);
                temp = new MapObjectData();
                temp.title = (TextView) row.findViewById(R.id.mapTitle);
                row.setTag(temp);
            } else {
                temp = (MapObjectData)row.getTag();
            }
            MapObject mapToBeDisplayed = mMaps.get(position);
            temp.title.setText(mapToBeDisplayed.getLabel());
            return row;
        }

        class MapObjectData
        {
            TextView title;
        }

    }

    /** Called when the user selects an item on the listview backing this MapListFragment */
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        MapObject selected = backingMapList.get(i);
        String id = selected.getID().toString();
        getActivity().getPreferences(Context.MODE_PRIVATE).edit().putString(MainActivity.PREF_CURRENT_MAP, id).commit();
        TakFragmentManager.switchToMap(getActivity(), selected);
    }

}
